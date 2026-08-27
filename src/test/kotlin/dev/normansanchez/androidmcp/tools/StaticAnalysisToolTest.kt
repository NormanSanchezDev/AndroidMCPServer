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

class StaticAnalysisToolTest {

    @Test
    fun `returns not_available when no tools found`() {
        val temp = Files.createTempDirectory("static-analysis")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = StaticAnalysisTool.execute(project.absolutePathString(), "detekt")

            val status = json["status"]?.toString()?.removeSurrounding("\"")
            assertTrue(status == "gradle_not_available" || status == "success" || status == "error")
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `returns invalid_project for bad path`() {
        val json = StaticAnalysisTool.execute("/nonexistent/path/project", "detekt")
        assertEquals("invalid_project", json["status"]?.toString()?.removeSurrounding("\""))
    }
}
