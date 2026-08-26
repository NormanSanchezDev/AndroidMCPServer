package dev.normansanchez.androidmcp.tools

import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import dev.normansanchez.androidmcp.graph.ModuleGraphParser
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile

object ModuleGraphTool {

    fun execute(projectRoot: String): kotlinx.serialization.json.JsonObject {
        val root = Path.of(projectRoot).normalize().toAbsolutePath()

        if (!Files.isDirectory(root)) {
            return buildJsonObject {
                put("status", "invalid_project")
                put("projectRoot", root.toString())
            }
        }

        val settingsFile = findSettingsFile(root)
            ?: return buildJsonObject {
                put("status", "not_available")
                put("projectRoot", root.toString())
                put("error", "No settings.gradle(.kts) found at project root")
            }

        val settingsContent = Files.readString(settingsFile)
        val (rootProjectName, modules) = ModuleGraphParser.parseSettings(settingsContent)

        if (modules.isEmpty()) {
            return buildJsonObject {
                put("status", "success")
                put("projectRoot", root.toString())
                rootProjectName?.let { put("rootProjectName", it) }
                put("modules", buildJsonArray {})
                put("edges", buildJsonArray {})
            }
        }

        val edges = modules.flatMap { module ->
            val moduleDir = root.resolve(module.removePrefix(":"))
            val buildFile = moduleDir.resolve("build.gradle.kts")
                .takeIf { it.isRegularFile() }
                ?: moduleDir.resolve("build.gradle").takeIf { it.isRegularFile() }

            if (buildFile == null) {
                emptyList()
            } else {
                try {
                    ModuleGraphParser.parseBuildFile(
                        moduleName = module.removePrefix(":"),
                        buildFileContent = Files.readString(buildFile)
                    )
                } catch (_: Exception) {
                    emptyList()
                }
            }
        }

        return buildJsonObject {
            put("status", "success")
            put("projectRoot", root.toString())
            rootProjectName?.let { put("rootProjectName", it) }
            put("moduleCount", modules.size)

            put(
                "modules",
                buildJsonArray { modules.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) } }
            )

            put(
                "edges",
                buildJsonArray {
                    edges.forEach { edge ->
                        add(
                            buildJsonObject {
                                put("from", edge.from)
                                put("to", edge.to)
                                edge.configuration?.let { put("configuration", it) }
                            }
                        )
                    }
                }
            )
        }
    }

    private fun findSettingsFile(root: Path): Path? =
        listOf("settings.gradle.kts", "settings.gradle")
            .firstNotNullOfOrNull { candidate ->
                root.resolve(candidate).takeIf(Files::isRegularFile)
            }
}
