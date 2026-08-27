package dev.normansanchez.androidmcp.tools

import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuildValidateToolTest {

    @Test
    fun `returns not_available when no gradlew found`() {
        val temp = Files.createTempDirectory("build-validate")
        try {
            Files.createDirectories(temp.resolve("project"))
            val json = BuildValidateTool.execute(temp.resolve("project").absolutePathString(), "app")

            val status = json["status"]?.toString()?.removeSurrounding("\"")
            assertTrue(status == "gradle_not_available" || status == "success")
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `returns invalid_project for bad path`() {
        val json = BuildValidateTool.execute("/nonexistent/path/project", "app")
        assertEquals("invalid_project", json["status"]?.toString()?.removeSurrounding("\""))
    }
}
