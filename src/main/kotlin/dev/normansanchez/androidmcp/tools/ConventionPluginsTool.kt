package dev.normansanchez.androidmcp.tools

import dev.normansanchez.androidmcp.gradle.ConventionPluginScanner
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.nio.file.Files
import java.nio.file.Path

object ConventionPluginsTool {

    fun execute(projectRoot: String): kotlinx.serialization.json.JsonObject {
        val root = Path.of(projectRoot).normalize().toAbsolutePath()

        if (!Files.isDirectory(root)) {
            return buildJsonObject {
                put("status", "invalid_project")
                put("projectRoot", root.toString())
            }
        }

        val plugins = ConventionPluginScanner.scan(root)

        if (plugins.isEmpty()) {
            return buildJsonObject {
                put("status", "not_available")
                put("projectRoot", root.toString())
                put("error", "No convention plugins found in build-logic/ or buildSrc/")
            }
        }

        return buildJsonObject {
            put("status", "success")
            put("projectRoot", root.toString())
            put("pluginCount", plugins.size)
            put(
                "plugins",
                buildJsonArray {
                    plugins.forEach { plugin ->
                        add(buildJsonObject {
                            put("id", plugin.id)
                            put("className", plugin.className)
                            put("file", plugin.filePath)
                            put(
                                "appliedBy",
                                buildJsonArray {
                                    plugin.appliedBy.forEach { add(JsonPrimitive(it)) }
                                }
                            )
                        })
                    }
                }
            )
        }
    }
}
