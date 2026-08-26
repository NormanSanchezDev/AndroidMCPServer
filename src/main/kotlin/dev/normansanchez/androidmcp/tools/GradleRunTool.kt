package dev.normansanchez.androidmcp.tools

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import dev.normansanchez.androidmcp.gradle.GradleCommandValidator
import dev.normansanchez.androidmcp.gradle.GradleWrapperLocator
import dev.normansanchez.androidmcp.process.ProcessExecutor
import java.nio.file.Files
import java.nio.file.Path

object GradleRunTool {

    const val MAX_OUTPUT_CHARS = 40_000

    fun execute(
        projectRoot: String,
        tasks: List<String>,
        flags: List<String> = emptyList(),
        timeoutSeconds: Long = 600
    ): kotlinx.serialization.json.JsonObject {
        val root = Path.of(projectRoot).normalize().toAbsolutePath()

        if (!Files.isDirectory(root)) {
            return buildJsonObject {
                put("status", "invalid_project")
                put("projectRoot", root.toString())
            }
        }

        if (tasks.isEmpty()) {
            return buildJsonObject {
                put("status", "invalid_request")
                put("error", "At least one task name is required")
            }
        }

        val invalidTask = tasks.firstOrNull { !GradleCommandValidator.validateTaskName(it) }
        if (invalidTask != null) {
            return buildJsonObject {
                put("status", "invalid_task")
                put("task", invalidTask)
            }
        }

        val invalidFlag = flags.firstOrNull { !GradleCommandValidator.validateFlag(it) }
        if (invalidFlag != null) {
            return buildJsonObject {
                put("status", "invalid_flag")
                put("flag", invalidFlag)
                put(
                    "allowedFlags",
                    kotlinx.serialization.json.buildJsonArray {
                        GradleCommandValidator.allowedFlags.forEach {
                            add(kotlinx.serialization.json.JsonPrimitive(it))
                        }
                    }
                )
            }
        }

        val wrapper = GradleWrapperLocator.findWrapper(root)
            ?: return buildJsonObject {
                put("status", "gradle_not_available")
                put("projectRoot", root.toString())
                put("error", "No gradlew wrapper found in project root")
            }

        val command = buildList {
            add(wrapper.toString())
            addAll(tasks)
            addAll(flags)
        }

        val result = ProcessExecutor.execute(
            command = command,
            workingDirectory = root.toFile(),
            timeoutSeconds = timeoutSeconds,
            maxCapturedChars = MAX_OUTPUT_CHARS
        )

        return buildJsonObject {
            put("status", when {
                result.timedOut -> "timeout"
                else -> "executed"
            })
            put("projectRoot", root.toString())
            put("command", result.command.joinToString(" "))
            put("exitCode", if (result.exitCode == null) kotlinx.serialization.json.JsonNull else JsonPrimitive(result.exitCode))
            put("success", result.success)
            put("durationMs", result.durationMs)
            put("stdout", result.stdout.take(MAX_OUTPUT_CHARS))
            put("stderr", result.stderr.take(MAX_OUTPUT_CHARS))
            put(
                "stdoutTruncated",
                result.stdout.length > MAX_OUTPUT_CHARS
            )
        }
    }
}
