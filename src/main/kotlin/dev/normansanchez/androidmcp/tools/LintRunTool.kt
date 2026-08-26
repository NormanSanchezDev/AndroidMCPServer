package dev.normansanchez.androidmcp.tools

import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import dev.normansanchez.androidmcp.gradle.GradleCommandValidator
import dev.normansanchez.androidmcp.gradle.GradleWrapperLocator
import dev.normansanchez.androidmcp.lint.LintXmlParser
import dev.normansanchez.androidmcp.process.ProcessExecutor
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name

object LintRunTool {

    private const val MAX_ISSUES_IN_REPORT = 200

    fun execute(
        projectRoot: String,
        module: String? = null,
        task: String? = null,
        trigger: Boolean = false,
        timeoutSeconds: Long = 900
    ): kotlinx.serialization.json.JsonObject {
        val root = Path.of(projectRoot).normalize().toAbsolutePath()

        if (!Files.isDirectory(root)) {
            return buildJsonObject {
                put("status", "invalid_project")
                put("projectRoot", root.toString())
            }
        }

        var moduleRoot = root
        if (!module.isNullOrBlank()) {
            moduleRoot = root.resolve(module.removePrefix(":"))
            if (!Files.isDirectory(moduleRoot)) {
                return buildJsonObject {
                    put("status", "invalid_module")
                    put("module", module)
                }
            }
        }

        var reportFile = latestLintReport(moduleRoot)

        if (reportFile == null && trigger) {
            val gradleStatus = runGradleLint(root, module, task, timeoutSeconds)
            gradleStatus?.let { return it }
            reportFile = latestLintReport(moduleRoot)
        }

        if (reportFile == null) {
            return buildJsonObject {
                put("status", "not_available")
                put("projectRoot", root.toString())
                put(
                    "searchedPath",
                    moduleRoot.resolve("build/reports").toString()
                )
                put(
                    "hint",
                    "No Android Lint XML report found. Run with trigger=true to execute the lint task first."
                )
            }
        }

        val content = try {
            Files.readString(reportFile)
        } catch (_: Exception) {
            null
        }

        if (content == null) {
            return buildJsonObject {
                put("status", "not_available")
                put("error", "Report file could not be read")
            }
        }

        val report = LintXmlParser.parse(content)
            ?: return buildJsonObject {
                put("status", "not_available")
                put("error", "Report file is not valid lint XML")
                put("report", root.relativize(reportFile).toString())
            }

        val fatalCount = report.countBySeverity("Fatal")
        val errorCount = report.countBySeverity("Error")
        val warningCount = report.countBySeverity("Warning")
        val infoCount = report.countBySeverity("Information")

        return buildJsonObject {
            put("status", "success")
            put("projectRoot", root.toString())
            put(
                "report",
                try {
                    root.relativize(reportFile).toString()
                } catch (_: Exception) {
                    reportFile.name
                }
            )
            report.lintVersion?.let { put("lintVersion", it) }
            put("issueCount", report.issues.size)
            put("fatal", fatalCount)
            put("errors", errorCount)
            put("warnings", warningCount)
            put("informational", infoCount)
            put("allClean", fatalCount == 0 && errorCount == 0 && warningCount == 0)

            put(
                "issues",
                buildJsonArray {
                    report.issues.take(MAX_ISSUES_IN_REPORT).forEach { issue ->
                        add(
                            buildJsonObject {
                                put("id", issue.id)
                                put("severity", issue.severity)
                                put("message", issue.message)
                                issue.category?.let { put("category", it) }
                                issue.file?.let { put("file", it) }
                                issue.line?.let { put("line", JsonPrimitive(it)) }
                            }
                        )
                    }
                }
            )
            put("issuesTruncated", report.issues.size > MAX_ISSUES_IN_REPORT)
        }
    }

    private fun latestLintReport(moduleRoot: Path): Path? {
        val reportsDir = moduleRoot.resolve("build/reports")
        if (!Files.isDirectory(reportsDir)) {
            return null
        }

        Files.list(reportsDir).use { entries ->
            return entries.filter {
                Files.isRegularFile(it) &&
                        it.name.startsWith("lint-results") &&
                        it.name.endsWith(".xml")
            }
                .sorted { a, b ->
                    Files.getLastModifiedTime(b).compareTo(Files.getLastModifiedTime(a))
                }
                .findFirst()
                .orElse(null)
        }
    }

    private fun runGradleLint(
        root: Path,
        module: String?,
        task: String?,
        timeoutSeconds: Long
    ): kotlinx.serialization.json.JsonObject? {
        val wrapper = GradleWrapperLocator.findWrapper(root)
            ?: return buildJsonObject {
                put("status", "gradle_not_available")
            }

        val gradleTask = task ?: defaultTaskFor(module)

        if (!GradleCommandValidator.validateTaskName(gradleTask)) {
            return buildJsonObject {
                put("status", "invalid_task")
                put("task", gradleTask)
            }
        }

        val result = ProcessExecutor.execute(
            command = listOf(wrapper.toString(), gradleTask, "--console=plain"),
            workingDirectory = root.toFile(),
            timeoutSeconds = timeoutSeconds
        )

        // Lint exits non-zero when issues reach abortOnError; the report is still valid evidence.
        if (result.timedOut) {
            return buildJsonObject {
                put("status", "timeout")
                put("command", result.command.joinToString(" "))
            }
        }

        return null
    }

    private fun defaultTaskFor(module: String?): String =
        if (module.isNullOrBlank()) "lint" else ":${module.removePrefix(":")}:lint"
}
