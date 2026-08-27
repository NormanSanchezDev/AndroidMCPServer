package dev.normansanchez.androidmcp.tools

import dev.normansanchez.androidmcp.gradle.GradleCommandValidator
import dev.normansanchez.androidmcp.gradle.GradleWrapperLocator
import dev.normansanchez.androidmcp.process.ProcessExecutor
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.nio.file.Files
import java.nio.file.Path

object BuildValidateTool {

    private val COMPILATION_TASKS = setOf(
        "compileDebugKotlin", "compileReleaseKotlin",
        "compileDebugJavaWithJavac", "compileReleaseJavaWithJavac",
        "compileDebugSources", "compileReleaseSources"
    )

    fun execute(
        projectRoot: String,
        module: String? = null,
        timeoutSeconds: Long = 600
    ): kotlinx.serialization.json.JsonObject {
        val root = Path.of(projectRoot).normalize().toAbsolutePath()

        if (!Files.isDirectory(root)) {
            return buildJsonObject {
                put("status", "invalid_project")
                put("projectRoot", root.toString())
            }
        }

        val wrapper = GradleWrapperLocator.findWrapper(root)
            ?: return buildJsonObject {
                put("status", "gradle_not_available")
                put("projectRoot", root.toString())
                put("error", "No gradlew wrapper found")
            }

        val modulePrefix = module?.removePrefix(":") ?: "app"
        val task = ":$modulePrefix:compileDebugKotlin"

        if (!GradleCommandValidator.validateTaskName(task.removePrefix(":"))) {
            return buildJsonObject {
                put("status", "invalid_request")
                put("error", "Invalid module name: $modulePrefix")
            }
        }

        val command = listOf(wrapper.toString(), task, "--console=plain")
        val result = ProcessExecutor.execute(
            command = command,
            workingDirectory = root.toFile(),
            timeoutSeconds = timeoutSeconds
        )

        if (result.timedOut) {
            return buildJsonObject {
                put("status", "timeout")
                put("projectRoot", root.toString())
                put("command", result.command.joinToString(" "))
                put("durationMs", result.durationMs)
            }
        }

        val warnings = extractWarnings(result.stderr)
        val errors = extractErrors(result.stderr)

        return buildJsonObject {
            put("status", "success")
            put("projectRoot", root.toString())
            put("command", result.command.joinToString(" "))
            put("exitCode", JsonPrimitive(result.exitCode ?: -1))
            put("durationMs", result.durationMs)
            put("success", result.exitCode == 0)
            put("warningCount", warnings.size)
            put("errorCount", errors.size)
            put(
                "warnings",
                buildJsonArray {
                    warnings.take(20).forEach { add(JsonPrimitive(it)) }
                }
            )
            put(
                "errors",
                buildJsonArray {
                    errors.take(20).forEach { add(JsonPrimitive(it)) }
                }
            )
        }
    }

    private fun extractWarnings(stderr: String): List<String> {
        return stderr.lines()
            .filter { it.contains("warning:", ignoreCase = true) || it.contains("w:") }
            .map { it.trim() }
            .distinct()
    }

    private fun extractErrors(stderr: String): List<String> {
        return stderr.lines()
            .filter { it.contains("error:", ignoreCase = true) || it.contains("e:") }
            .map { it.trim() }
            .distinct()
    }
}
