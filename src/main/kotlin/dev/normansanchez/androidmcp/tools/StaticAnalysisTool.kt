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

object StaticAnalysisTool {

    private val DETEKT_TASKS = listOf("detekt")
    private val KTLINT_TASKS = listOf("ktlintCheck", "ktlintFormat")
    private val KOVER_TASKS = listOf("koverHtmlReport", "koverXmlReport")

    fun execute(
        projectRoot: String,
        module: String? = null,
        tools: List<String>? = null,
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
            }

        val modulePrefix = module?.removePrefix(":")
        val requestedTools = tools ?: listOf("detekt", "ktlint")

        val results = mutableMapOf<String, ToolResult>()

        for (tool in requestedTools) {
            val taskName = when (tool) {
                "detekt" -> "detekt"
                "ktlint" -> "ktlintCheck"
                "kover" -> "koverHtmlReport"
                else -> continue
            }

            val fullTask = if (modulePrefix != null) ":$modulePrefix:$taskName" else taskName

            if (!GradleCommandValidator.validateTaskName(fullTask.removePrefix(":"))) continue

            val command = listOf(wrapper.toString(), fullTask, "--console=plain")
            val result = ProcessExecutor.execute(
                command = command,
                workingDirectory = root.toFile(),
                timeoutSeconds = timeoutSeconds
            )

            results[tool] = ToolResult(
                exitCode = result.exitCode ?: -1,
                stdout = result.stdout,
                stderr = result.stderr,
                durationMs = result.durationMs,
                timedOut = result.timedOut
            )
        }

        return buildJsonObject {
            put("status", "success")
            put("projectRoot", root.toString())
            put(
                "tools",
                buildJsonArray {
                    results.forEach { (tool, result) ->
                        add(buildJsonObject {
                            put("name", tool)
                            put("exitCode", JsonPrimitive(result.exitCode))
                            put("durationMs", result.durationMs)
                            put("success", result.exitCode == 0 && !result.timedOut)
                            put("timedOut", result.timedOut)
                            put("outputLength", result.stdout.length + result.stderr.length)
                        })
                    }
                }
            )
        }
    }

    private data class ToolResult(
        val exitCode: Int,
        val stdout: String,
        val stderr: String,
        val durationMs: Long,
        val timedOut: Boolean
    )
}
