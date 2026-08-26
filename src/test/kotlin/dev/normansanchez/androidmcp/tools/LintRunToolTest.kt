package dev.normansanchez.androidmcp.tools

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.absolutePathString
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class LintRunToolTest {

    @Test
    fun `aggregates lint report by severity with issue details`() {
        val temp = Files.createTempDirectory("lint-run")
        try {
            val project = temp.resolve("android-project")
            val reportsDir = project.resolve("build/reports")
            Files.createDirectories(reportsDir)

            copyResource("fixtures/lint/lint-results-debug.xml", reportsDir)

            val json = LintRunTool.execute(project.absolutePathString())

            assertEquals("success", json["status"]!!.jsonPrimitive.content)
            assertEquals(3, json["issueCount"]!!.jsonPrimitive.int)
            assertEquals(1, json["errors"]!!.jsonPrimitive.int)
            assertEquals(2, json["warnings"]!!.jsonPrimitive.int)
            assertEquals(false, json["allClean"]!!.jsonPrimitive.boolean)
            assertEquals("lint 8.7.0", json["lintVersion"]!!.jsonPrimitive.content)

            val issues = json["issues"]!!.jsonArray.map { it.jsonObject }
            val hardcoded = issues.first { it["id"]!!.jsonPrimitive.content == "HardcodedText" }
            assertEquals(17, hardcoded["line"]!!.jsonPrimitive.int)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `returns not_available without report and trigger disabled`() {
        val temp = Files.createTempDirectory("lint-empty")
        try {
            val project = temp.resolve("clean-project")
            Files.createDirectories(project)

            val json = LintRunTool.execute(project.absolutePathString())

            assertEquals("not_available", json["status"]!!.jsonPrimitive.content)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    private fun copyResource(resourcePath: String, targetDirectory: java.nio.file.Path) {
        val source = LintRunToolTest::class.java.classLoader.getResource(resourcePath)!!
        val sourcePath = java.nio.file.Path.of(source.toURI())
        val target = targetDirectory.resolve(sourcePath.fileName.toString())
        Files.copy(sourcePath, target, StandardCopyOption.REPLACE_EXISTING)
    }
}
