package latticeMCP.tools

import dev.lattice.androidmcp.latticeMCP.model.AndroidModuleEvidence
import dev.lattice.androidmcp.latticeMCP.model.AndroidModuleType
import kotlinx.serialization.json.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile

object ProjectInspectTool {
    fun execute(projectRoot: String): JsonObject {
        val root = Path.of(projectRoot)
            .normalize()
            .toAbsolutePath()

        val exists = Files.exists(root)
        val isDirectory = Files.isDirectory(root)

        if (!exists || !isDirectory) {
            return buildJsonObject {
                put("status", "invalid_project")
                put("projectRoot", root.toString())
                put("exists", exists)
                put("isDirectory", isDirectory)
            }
        }

        val settingsFile = findSettingsFile(root)
        val modules = findModules(root)
        val androidModules = modules.filter {
            it.type == AndroidModuleType.APPLICATION ||
                    it.type == AndroidModuleType.LIBRARY
        }

        return buildJsonObject {
            put("status", "success")
            put("projectRoot", root.toString())
            put("gradleProject", settingsFile != null)
            put(
                "settingsFile",
                settingsFile?.let {
                    JsonPrimitive(
                        root.relativize(it).toString()
                    )
                } ?: JsonPrimitive(null)
            )

            put("androidProject", androidModules.isNotEmpty())
            put(
                "modules",
                buildJsonArray {
                    modules.forEach { module ->
                        add(
                            buildJsonObject {
                                put("name", module.name)
                                put("path", module.path)
                                put("type", module.type.value)
                                put("buildFile", module.buildFile)
                                put(
                                    "manifest",
                                    module.manifest?.let {
                                        JsonPrimitive(it)
                                    } ?: JsonPrimitive(null)
                                )
                            }
                        )
                    }
                }
            )
            put(
                "evidence",
                buildEvidence(androidModules)
            )
        }
    }

    private fun findSettingsFile(root: Path): Path? {
        val kotlinDsl = root.resolve("settings.gradle.kts")
        if (Files.isRegularFile(kotlinDsl)) {
            return kotlinDsl
        }

        val groovyDsl = root.resolve("settings.gradle")
        if (Files.isRegularFile(groovyDsl)) {
            return groovyDsl
        }
        return null

    }

    private fun findModules(root: Path): List<AndroidModuleEvidence> {
        return Files.walk(root, 3).use { paths ->
            paths
                .filter { path ->
                    path.isRegularFile() && (path.fileName.toString() == "build.gradle.kts" || path.fileName.toString() == "build.gradle")
                }
                .filter { path ->
                    path.parent != root
                }
                .map { buildFile ->
                    inspectModule(
                        root = root,
                        buildFile = buildFile
                    )
                }
                .toList()

        }

    }

    private fun inspectModule(
        root: Path,
        buildFile: Path
    ): AndroidModuleEvidence {
        val moduleRoot = buildFile.parent
        val content = Files.readString(buildFile)
        val type = when {
            containsApplicationPlugin(content) ->
                AndroidModuleType.APPLICATION
            containsLibraryPlugin(content) ->
                AndroidModuleType.LIBRARY
            else -> AndroidModuleType.UNKNOWN
        }

        val manifest = moduleRoot
            .resolve("src/main/AndroidManifest.xml")
            .takeIf(Files::isRegularFile)

        return AndroidModuleEvidence(
            name = moduleRoot.fileName?.toString() ?: ".",
            path = root.relativize(moduleRoot).toString(),
            type = type,
            buildFile = root.relativize(buildFile).toString(),
            manifest = manifest?.let { root.relativize(it).toString() }
        )
    }

    private fun containsApplicationPlugin(
        content: String
    ): Boolean {

        return content.contains("com.android.application") ||
                content.contains("id(\"com.android.application\")") ||
                content.contains("id 'com.android.application'")
    }

    private fun containsLibraryPlugin(
        content: String
    ): Boolean {

        return content.contains("com.android.library") ||
                content.contains("id(\"com.android.library\")") ||
                content.contains("id 'com.android.library'")
    }

    private fun buildEvidence(
        modules: List<AndroidModuleEvidence>
    ): JsonArray {

        return buildJsonArray {
            modules.forEach { module ->
                add(
                    buildJsonObject {
                        put("type", "android_gradle_plugin")
                        put("module", module.name)
                        put("file", module.buildFile)
                        put("value", module.type.pluginId)
                    }
                )
                module.manifest?.let { manifest ->
                    add(
                        buildJsonObject {
                            put("type", "android_manifest")
                            put("module", module.name)
                            put("file", manifest)
                        }
                    )
                }
            }
        }
    }
}