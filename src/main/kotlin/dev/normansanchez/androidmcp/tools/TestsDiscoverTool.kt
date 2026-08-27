package dev.normansanchez.androidmcp.tools

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name

object TestsDiscoverTool {

    private val TEST_PATTERNS = listOf("Test.kt", "Tests.kt", "Spec.kt")

    fun execute(projectRoot: String, className: String): kotlinx.serialization.json.JsonObject {
        val root = Path.of(projectRoot).normalize().toAbsolutePath()

        if (!Files.isDirectory(root)) {
            return buildJsonObject {
                put("status", "invalid_project")
                put("projectRoot", root.toString())
            }
        }

        if (!isValidIdentifier(className)) {
            return buildJsonObject {
                put("status", "invalid_request")
                put("error", "className must be a valid Kotlin identifier")
            }
        }

        val testFiles = findTestFiles(root, className)
        val matches = testFiles.mapNotNull { testFile ->
            val content = try { Files.readString(testFile) } catch (_: Exception) { return@mapNotNull null }
            val referencesProduction = content.contains(className)
            val isUnitTest = testFile.toString().contains("/test/")
            val isInstrumentedTest = testFile.toString().contains("/androidTest/")

            TestMatch(
                path = root.relativize(testFile).toString(),
                referencesProduction = referencesProduction,
                testType = when {
                    isInstrumentedTest -> "instrumented"
                    isUnitTest -> "unit"
                    else -> "unknown"
                },
                lineCount = content.lines().size
            )
        }

        return buildJsonObject {
            put("status", "success")
            put("projectRoot", root.toString())
            put("className", className)
            put("testCount", matches.size)
            put(
                "tests",
                buildJsonArray {
                    matches.forEach { m ->
                        add(buildJsonObject {
                            put("file", m.path)
                            put("type", m.testType)
                            put("referencesProduction", m.referencesProduction)
                            put("lineCount", m.lineCount)
                        })
                    }
                }
            )
        }
    }

    private fun findTestFiles(root: Path, className: String): List<Path> {
        val results = mutableListOf<Path>()

        val possibleTestNames = TEST_PATTERNS.map { className + it }
        val possibleTestNamesLower = TEST_PATTERNS.map { className.lowercase() + it }

        Files.walk(root, 8).use { paths ->
            paths.filter { Files.isRegularFile(it) && it.name.endsWith(".kt") }
                .forEach { file ->
                    val fileName = file.fileName.toString()
                    if (possibleTestNames.any { fileName == it } ||
                        possibleTestNamesLower.any { fileName.equals(it, ignoreCase = true) }) {
                        results.add(file)
                    }
                }
        }

        return results.sorted()
    }

    private fun isValidIdentifier(name: String): Boolean =
        name.matches(Regex("^[A-Za-z_][A-Za-z0-9_]*$"))

    private data class TestMatch(
        val path: String,
        val referencesProduction: Boolean,
        val testType: String,
        val lineCount: Int
    )
}
