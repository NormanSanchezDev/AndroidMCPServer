package dev.normansanchez.androidmcp.tools

import dev.normansanchez.androidmcp.gradle.VersionCatalogParser
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.nio.file.Files
import java.nio.file.Path

object VersionCatalogTool {

    fun execute(projectRoot: String): kotlinx.serialization.json.JsonObject {
        val root = Path.of(projectRoot).normalize().toAbsolutePath()

        if (!Files.isDirectory(root)) {
            return buildJsonObject {
                put("status", "invalid_project")
                put("projectRoot", root.toString())
            }
        }

        val catalog = VersionCatalogParser.parse(root)
            ?: return buildJsonObject {
                put("status", "not_available")
                put("projectRoot", root.toString())
                put("error", "No gradle/libs.versions.toml found")
            }

        return buildJsonObject {
            put("status", "success")
            put("projectRoot", root.toString())
            put(
                "versions",
                buildJsonArray {
                    catalog.versions.forEach { v ->
                        add(buildJsonObject {
                            put("name", v.name)
                            put("value", v.value)
                        })
                    }
                }
            )
            put(
                "libraries",
                buildJsonArray {
                    catalog.libraries.forEach { lib ->
                        add(buildJsonObject {
                            put("alias", lib.alias)
                            put("group", lib.group)
                            put("name", lib.name)
                            put("version", lib.version?.let { JsonPrimitive(it) } ?: JsonPrimitive(null as String?))
                            put("versionRef", lib.versionRef?.let { JsonPrimitive(it) } ?: JsonPrimitive(null as String?))
                        })
                    }
                }
            )
            put(
                "plugins",
                buildJsonArray {
                    catalog.plugins.forEach { plugin ->
                        add(buildJsonObject {
                            put("id", plugin.id)
                            put("version", plugin.version?.let { JsonPrimitive(it) } ?: JsonPrimitive(null as String?))
                            put("versionRef", plugin.versionRef?.let { JsonPrimitive(it) } ?: JsonPrimitive(null as String?))
                        })
                    }
                }
            )
        }
    }
}
