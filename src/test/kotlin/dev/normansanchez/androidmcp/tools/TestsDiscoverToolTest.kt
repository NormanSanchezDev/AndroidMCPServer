package dev.normansanchez.androidmcp.tools

import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestsDiscoverToolTest {

    @Test
    fun `finds test for MainActivity`() {
        val temp = Files.createTempDirectory("tests-discover")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = TestsDiscoverTool.execute(project.absolutePathString(), "MainActivity")

            assertEquals("success", json["status"]?.toString()?.removeSurrounding("\""))
            assertEquals(true, (json["testCount"]?.toString()?.toIntOrNull() ?: 0) >= 1)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `rejects invalid class name`() {
        val temp = Files.createTempDirectory("tests-discover-invalid")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = TestsDiscoverTool.execute(project.absolutePathString(), "not valid;identifier")

            assertEquals("invalid_request", json["status"]?.toString()?.removeSurrounding("\""))
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}
