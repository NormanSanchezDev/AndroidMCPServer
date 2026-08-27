package dev.normansanchez.androidmcp.tools

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GradleConfigToolTest {

    @Test
    fun `extracts config from build file`() {
        val temp = Files.createTempDirectory("gradle-config")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = GradleConfigTool.execute(project.absolutePathString())

            assertEquals("success", json["status"]?.toString()?.removeSurrounding("\""))
            assertEquals(35, json["compileSdk"]?.toString()?.toIntOrNull())
            assertEquals(26, json["minSdk"]?.toString()?.toIntOrNull())
            assertEquals(35, json["targetSdk"]?.toString()?.toIntOrNull())
            assertEquals(true, json["composeEnabled"]?.toString()?.toBoolean())
            val plugins = json["plugins"]?.toString() ?: ""
            assertTrue(plugins.contains("android-application") ||
                    plugins.contains("com.android.application") ||
                    plugins.contains("kotlin.android") ||
                    plugins.contains("hilt"))
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `returns not_found for missing module`() {
        val temp = Files.createTempDirectory("gradle-config-missing")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = GradleConfigTool.execute(project.absolutePathString(), "nonexistent")

            assertEquals("module_not_found", json["status"]?.toString()?.removeSurrounding("\""))
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `returns invalid_project for bad path`() {
        val json = GradleConfigTool.execute("/nonexistent/path")
        assertEquals("invalid_project", json["status"]?.toString()?.removeSurrounding("\""))
    }
}
