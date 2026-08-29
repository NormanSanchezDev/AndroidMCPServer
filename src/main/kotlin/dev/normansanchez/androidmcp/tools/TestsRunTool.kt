package dev.normansanchez.androidmcp.tools

import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import dev.normansanchez.androidmcp.gradle.GradleCommandValidator
import dev.normansanchez.androidmcp.gradle.GradleWrapperLocator
import dev.normansanchez.androidmcp.junit.JunitXmlParser
import dev.normansanchez.androidmcp.process.ProcessExecutor
import dev.normansanchez.androidmcp.util.resolveModuleOrNull
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import kotlin.io.path.absolutePathString
import kotlin.io.path.name
import kotlin.io.path.relativeToOrNull

object TestsRunTool {

    private val REPORT_DIRECTORIES = listOf(
        "build/test-results/testDebugUnitTest",
        "build/test-results/testReleaseUnitTest",
        "build/test-results/test"
    )

    fun execute(
        projectRoot: String,
        module: String? = null,
        task: String? = null,
        trigger: Boolean = false,
        timeoutSeconds: Long = 600
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
            moduleRoot = root.resolveModuleOrNull(module)
                ?: return buildJsonObject {
                    put("status", "invalid_module")
                    put("projectRoot", root.toString())
                    put("module", module)
                }
            if (!Files.isDirectory(moduleRoot)) {
                return buildJsonObject {
                    put("status", "invalid_module")
                    put("projectRoot", root.toString())
                    put("module", module)
                }
            }
        }

        var suites = collectSuites(root, moduleRoot)

        if (suites.isEmpty() && trigger) {
            val gradleStatus = runGradleTests(root, module, task, timeoutSeconds)
            gradleStatus?.let { return it }
            suites = collectSuites(root, moduleRoot)
        }

        if (suites.isEmpty()) {
            return buildJsonObject {
                put("status", "not_available")
                put("projectRoot", root.toString())
                put(
                    "searchedPaths",
                    buildJsonArray {
                        searchedDirectories(moduleRoot).forEach {
                            add(JsonPrimitive(it))
                        }
                    }
                )
                put(
                    "hint",
                    "No JUnit XML reports found. Run the tests first (trigger=true) or execute the Gradle test task manually."
                )
            }
        }

        val totalTests = suites.sumOf { it.tests }
        val totalFailures = suites.sumOf { it.failures }
        val totalErrors = suites.sumOf { it.errors }
        val totalSkipped = suites.sumOf { it.skipped }
        val totalPassed = totalTests - totalFailures - totalErrors - totalSkipped

        return buildJsonObject {
            put("status", "success")
            put("projectRoot", root.toString())
            put("reportCount", suites.size)
            put("totalTests", totalTests)
            put("passed", totalPassed)
            put("failed", totalFailures + totalErrors)
            put("skipped", totalSkipped)
            put("allPassed", totalFailures == 0 && totalErrors == 0)

            put(
                "suites",
                buildJsonArray {
                    suites.forEach { suite ->
                        add(
                            buildJsonObject {
                                put("name", suite.name)
                                put("tests", suite.tests)
                                put("failures", suite.failures)
                                put("errors", suite.errors)
                                put("skipped", suite.skipped)
                                suite.timeSeconds?.let { put("timeSeconds", JsonPrimitive(it)) }

                                val failures = suite.testCases.filter { it.failureMessage != null }
                                if (failures.isNotEmpty()) {
                                    put(
                                        "failuresDetail",
                                        buildJsonArray {
                                            failures.forEach { failure ->
                                                add(
                                                    buildJsonObject {
                                                        put("classname", failure.classname)
                                                        put("test", failure.name)
                                                        put("message", failure.failureMessage ?: "")
                                                        failure.failureType?.let { put("type", it) }
                                                    }
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        )
                    }
                }
            )
        }
    }

    private fun collectSuites(
        root: Path,
        moduleRoot: Path
    ): List<dev.normansanchez.androidmcp.junit.JunitTestSuite> {
        val parsed = mutableListOf<Pair<Path, dev.normansanchez.androidmcp.junit.JunitTestSuite>>()

        for (directory in searchedDirectories(moduleRoot)) {
            val dir = Path.of(directory)
            if (!Files.isDirectory(dir)) continue

            Files.list(dir).use { entries ->
                entries.filter { it.name.startsWith("TEST-") && it.name.endsWith(".xml") }
                    .sorted()
                    .collect(Collectors.toList())
                    .forEach { file ->
                        val content = try {
                            Files.readString(file)
                        } catch (_: Exception) {
                            null
                        } ?: return@forEach

                        val suite = JunitXmlParser.parse(content) ?: return@forEach
                        parsed.add(file to suite)
                    }
            }
        }

        return parsed.sortedBy { it.first.toString() }.map { it.second }
    }

    private fun searchedDirectories(moduleRoot: Path): List<String> =
        REPORT_DIRECTORIES.map { moduleRoot.resolve(it) }
            .filter { Files.isDirectory(it) }
            .map { it.toString() }

    private fun runGradleTests(
        root: Path,
        module: String?,
        task: String?,
        timeoutSeconds: Long
    ): kotlinx.serialization.json.JsonObject? {
        val wrapper = GradleWrapperLocator.findWrapper(root)
            ?: return buildJsonObject {
                put("status", "gradle_not_available")
                put("projectRoot", root.toString())
            }

        val gradleTask = task ?: defaultTaskFor(module)

        if (!GradleCommandValidator.validateTaskName(gradleTask)) {
            return buildJsonObject {
                put("status", "invalid_task")
                put("task", gradleTask)
            }
        }

        val command = buildList {
            add(wrapper.toString())
            add(gradleTask)
            add("--console=plain")
        }

        val result = ProcessExecutor.execute(
            command = command,
            workingDirectory = root.toFile(),
            timeoutSeconds = timeoutSeconds
        )

        if (result.timedOut || result.exitCode != 0) {
            return buildJsonObject {
                put("status", if (result.timedOut) "timeout" else "gradle_error")
                put("command", result.command.joinToString(" "))
                put("exitCode", result.exitCode?.let { JsonPrimitive(it) } ?: JsonNull)
                put("stderr", result.stderr.take(20_000))
            }
        }

        return null
    }

    private fun defaultTaskFor(module: String?): String =
        if (module.isNullOrBlank()) "test" else ":${module.removePrefix(":")}:test"
}
