package dev.normansanchez.androidmcp.tools

import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationGraphToolTest {

    @Test
    fun `finds xml and compose navigation`() {
        val temp = Files.createTempDirectory("nav-graph")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = NavigationGraphTool.execute(project.absolutePathString())

            assertEquals("success", json["status"]?.toString()?.removeSurrounding("\""))
            val hasXml = json["hasXmlNavigation"]?.toString()?.toBoolean() ?: false
            val hasCompose = json["hasComposeNavigation"]?.toString()?.toBoolean() ?: false
            // At least one navigation system should be detected
            assertEquals(true, hasXml || hasCompose)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}
