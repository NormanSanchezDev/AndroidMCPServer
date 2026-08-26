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

class TestsRunToolTest {

    @Test
    fun `aggregates junit reports from build test-results`() {
        val temp = Files.createTempDirectory("tests-run")
        try {
            val project = temp.resolve("demo-project")
            val resultsDir = project.resolve("build/test-results/test")
            Files.createDirectories(resultsDir)

            copyResource("fixtures/junit/TEST-com.corporate.app.ParserTest.xml", resultsDir)
            copyResource("fixtures/junit/TEST-com.corporate.data.RepositoryTest.xml", resultsDir)

            val json = TestsRunTool.execute(project.absolutePathString())

            assertEquals("success", json["status"]!!.jsonPrimitive.content)
            assertEquals(5, json["totalTests"]!!.jsonPrimitive.int)
            assertEquals(1, json["failed"]!!.jsonPrimitive.int)
            assertEquals(1, json["skipped"]!!.jsonPrimitive.int)
            assertEquals(3, json["passed"]!!.jsonPrimitive.int)
            assertEquals(false, json["allPassed"]!!.jsonPrimitive.boolean)

            val suites = json["suites"]!!.jsonArray.map { it.jsonObject }
            assertEquals(2, suites.size)

            val failingSuite = suites.first { it["failures"]!!.jsonPrimitive.int == 1 }
            val failureDetail =
                failingSuite["failuresDetail"]!!.jsonArray.first().jsonObject
            assertEquals("propagates network failure()", failureDetail["test"]!!.jsonPrimitive.content)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `returns not_available when no reports exist`() {
        val temp = Files.createTempDirectory("tests-empty")
        try {
            val project = temp.resolve("empty-project")
            Files.createDirectories(project)

            val json = TestsRunTool.execute(project.absolutePathString())

            assertEquals("not_available", json["status"]!!.jsonPrimitive.content)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    private fun copyResource(resourcePath: String, targetDirectory: java.nio.file.Path) {
        val source = TestsRunToolTest::class.java.classLoader.getResource(resourcePath)!!
        val sourcePath = java.nio.file.Path.of(source.toURI())
        val target = targetDirectory.resolve(sourcePath.fileName.toString())
        Files.copy(sourcePath, target, StandardCopyOption.REPLACE_EXISTING)
    }
}
