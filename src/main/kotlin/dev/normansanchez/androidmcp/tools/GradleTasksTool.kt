package dev.normansanchez.androidmcp.tools

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import dev.normansanchez.androidmcp.gradle.GradleTasksParser
import dev.normansanchez.androidmcp.process.ProcessExecutor
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString

object GradleTasksTool {

    fun execute(
        projectRoot: String,
        module: String? = null,
        timeoutSeconds: Long = 300
    ): kotlinx.serialization.json.JsonObject {
        val root = Path.of(projectRoot).normalize().toAbsolutePath()

        if (!Files.isDirectory(root)) {
            return buildJsonObject {
                put("status", "invalid_project")
                put("projectRoot", root.toString())
            }
        }

        val wrapper = dev.normansanchez.androidmcp.gradle.GradleWrapperLocator.findWrapper(root)
            ?: return buildJsonObject {
                put("status", "gradle_not_available")
                put("projectRoot", root.toString())
                put("error", "No gradlew wrapper found in project root")
            }

        val target = module?.removePrefix(":")
        val command = buildList {
            add(wrapper.toString())
            add(if (target.isNullOrBlank()) "tasks" else ":$target:tasks")
            add("--all")
        }

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

        if (result.exitCode != 0) {
            return buildJsonObject {
                put("status", "gradle_error")
                put("projectRoot", root.toString())
                put("command", result.command.joinToString(" "))
                put("exitCode", JsonPrimitive(result.exitCode))
                put("stderr", result.stderr.take(MAX_STDERR))
            }
        }

        val tasks = GradleTasksParser.parse(result.stdout)

        return buildJsonObject {
            put("status", "success")
            put("projectRoot", root.toString())
            put("command", result.command.joinToString(" "))
            put("durationMs", result.durationMs)
            put("taskCount", tasks.size)
            put(
                "tasks",
                buildJsonArray {
                    tasks.forEach { task ->
                        add(
                            buildJsonObject {
                                put("name", task.name)
                                put("group", task.group)
                                task.description?.let { put("description", it) }
                            }
                        )
                    }
                }
            )
        }
    }

    private const val MAX_STDERR = 20_000
}
