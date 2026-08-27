package dev.normansanchez.androidmcp.tools

import dev.normansanchez.androidmcp.gradle.GradlePropertiesParser
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.nio.file.Files
import java.nio.file.Path

object GradleConfigTool {

    fun execute(projectRoot: String, module: String? = null): kotlinx.serialization.json.JsonObject {
        val root = Path.of(projectRoot).normalize().toAbsolutePath()

        if (!Files.isDirectory(root)) {
            return buildJsonObject {
                put("status", "invalid_project")
                put("projectRoot", root.toString())
            }
        }

        val moduleDir = if (module.isNullOrBlank()) {
            root.resolve("app").takeIf { Files.isDirectory(it) } ?: root
        } else {
            root.resolve(module.removePrefix(":"))
        }

        if (!Files.isDirectory(moduleDir)) {
            return buildJsonObject {
                put("status", "module_not_found")
                put("projectRoot", root.toString())
                put("module", module ?: "app")
            }
        }

        val config = GradlePropertiesParser.parse(moduleDir)
        val enriched = GradlePropertiesParser.enrichWithProjectProps(config, root)

        return buildJsonObject {
            put("status", "success")
            put("projectRoot", root.toString())
            put("module", root.relativize(moduleDir).toString())
            put(
                "plugins",
                buildJsonArray {
                    enriched.appliedPlugins.forEach { add(JsonPrimitive(it)) }
                }
            )
            put("compileSdk", enriched.compileSdk?.let { JsonPrimitive(it) } ?: JsonPrimitive(null as Int?))
            put("minSdk", enriched.minSdk?.let { JsonPrimitive(it) } ?: JsonPrimitive(null as Int?))
            put("targetSdk", enriched.targetSdk?.let { JsonPrimitive(it) } ?: JsonPrimitive(null as Int?))
            put("composeEnabled", enriched.composeEnabled)
            put(
                "buildTypes",
                buildJsonArray {
                    enriched.buildTypes.forEach { add(JsonPrimitive(it)) }
                }
            )
            put(
                "productFlavors",
                buildJsonArray {
                    enriched.productFlavors.forEach { add(JsonPrimitive(it)) }
                }
            )
            put("javaVersion", enriched.javaVersion?.let { JsonPrimitive(it) } ?: JsonPrimitive(null as String?))
            put("namespace", enriched.namespace?.let { JsonPrimitive(it) } ?: JsonPrimitive(null as String?))
            put("kotlinVersion", enriched.kotlinVersion?.let { JsonPrimitive(it) } ?: JsonPrimitive(null as String?))
            put("androidGradlePluginVersion", enriched.androidGradlePluginVersion?.let { JsonPrimitive(it) } ?: JsonPrimitive(null as String?))
        }
    }
}
