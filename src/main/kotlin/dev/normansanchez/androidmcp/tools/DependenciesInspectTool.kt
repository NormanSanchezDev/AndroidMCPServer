package dev.normansanchez.androidmcp.tools

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import dev.normansanchez.androidmcp.dependencies.GradleDependenciesTreeParser
import dev.normansanchez.androidmcp.gradle.GradleCommandValidator
import dev.normansanchez.androidmcp.gradle.GradleWrapperLocator
import dev.normansanchez.androidmcp.process.ProcessExecutor
import java.nio.file.Files
import java.nio.file.Path

object DependenciesInspectTool {

    fun execute(
        projectRoot: String,
        module: String? = null,
        configuration: String? = null,
        timeoutSeconds: Long = 300
    ): kotlinx.serialization.json.JsonObject {
        val root = Path.of(projectRoot).normalize().toAbsolutePath()

        if (!Files.isDirectory(root)) {
            return buildJsonObject {
                put("status", "invalid_project")
            }
        }

        val wrapper = GradleWrapperLocator.findWrapper(root)
            ?: return buildJsonObject {
                put("status", "gradle_not_available")
                put("projectRoot", root.toString())
            }

        if (!GradleCommandValidator.validateTaskName(module?.let { ":${it.removePrefix(":")}:dependencies" } ?: "dependencies")) {
            return buildJsonObject {
                put("status", "invalid_module")
                put("module", module)
            }
        }

        if (configuration != null && !Regex("^[a-zA-Z][a-zA-Z0-9_]*$").matches(configuration)) {
            return buildJsonObject {
                put("status", "invalid_configuration")
                put("configuration", configuration)
            }
        }

        val command = buildList {
            add(wrapper.toString())
            add(module?.let { ":${it.removePrefix(":")}:dependencies" } ?: "dependencies")
            if (configuration != null) {
                add("--configuration")
                add(configuration)
            }
            add("--console=plain")
        }

        val result = ProcessExecutor.execute(
            command = command,
            workingDirectory = root.toFile(),
            timeoutSeconds = timeoutSeconds
        )

        if (result.timedOut) {
            return buildJsonObject {
                put("status", "timeout")
                put("command", result.command.joinToString(" "))
            }
        }

        if (result.exitCode != 0) {
            return buildJsonObject {
                put("status", "gradle_error")
                put("exitCode", JsonPrimitive(result.exitCode))
                put("stderr", result.stderr.take(20_000))
            }
        }

        val configurations = GradleDependenciesTreeParser.parse(result.stdout)

        return buildJsonObject {
            put("status", "success")
            put("projectRoot", root.toString())
            put(
                "module",
                module?.removePrefix(":") ?: "<root>"
            )
            put("command", result.command.joinToString(" "))
            put(
                "configurations",
                buildJsonArray {
                    configurations.forEach { config ->
                        add(
                            buildJsonObject {
                                put("name", config.configuration)
                                config.description?.let { put("description", it) }
                                put("dependencyCount", config.dependencies.size)

                                val conflicts =
                                    config.dependencies.count { it.omittedByConflict }
                                put("conflicts", conflicts)

                                put(
                                    "directDependencies",
                                    buildJsonArray {
                                        config.dependencies
                                            .filter { !it.isProjectDependency }
                                            .forEach { dep ->
                                                add(
                                                    buildJsonObject {
                                                        put(
                                                            "coordinate",
                                                            listOfNotNull(
                                                                dep.group,
                                                                dep.name
                                                            ).joinToString(":") +
                                                                    (dep.version?.let { ":$it" } ?: "")
                                                        )
                                                        dep.requestedVersion?.let {
                                                            put("requestedVersion", it)
                                                        }
                                                        if (dep.omittedByConflict) {
                                                            put("omittedByConflict", true)
                                                        }
                                                        if (dep.repeatedSubtreeOmitted) {
                                                            put("repeatedSubtreeOmitted", true)
                                                        }
                                                    }
                                                )
                                            }
                                    }
                                )
                            }
                        )
                    }
                }
            )
        }
    }
}
