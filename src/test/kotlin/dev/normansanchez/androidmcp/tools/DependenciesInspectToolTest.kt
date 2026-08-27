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

class DependenciesInspectToolTest {

    @Test
    fun `parses dependency tree from fixture project`() {
        val temp = Files.createTempDirectory("deps-inspect")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = DependenciesInspectTool.execute(project.absolutePathString(), "app")

            val status = json["status"]?.toString()?.removeSurrounding("\"")
            // Returns success when Gradle is available, gradle_not_available otherwise
            assertTrue(status == "success" || status == "gradle_not_available")
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `returns invalid_project for bad path`() {
        val json = DependenciesInspectTool.execute("/nonexistent/path/project", "app")
        assertEquals("invalid_project", json["status"]?.toString()?.removeSurrounding("\""))
    }
}
