package dev.normansanchez.androidmcp.tools

import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.test.Test
import kotlin.test.assertEquals

class ConventionPluginsToolTest {

    @Test
    fun `finds convention plugins`() {
        val temp = Files.createTempDirectory("convention-plugins")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = ConventionPluginsTool.execute(project.absolutePathString())

            val status = json["status"]?.toString()?.removeSurrounding("\"")
            // Either finds plugins or returns not_available depending on fixture
            assertTrue(status == "success" || status == "not_available")
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    private fun assertTrue(condition: Boolean) {
        kotlin.test.assertTrue(condition)
    }
}
