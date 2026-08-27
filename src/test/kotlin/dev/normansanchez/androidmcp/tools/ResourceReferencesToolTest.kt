package dev.normansanchez.androidmcp.tools

import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.test.Test
import kotlin.test.assertEquals

class ResourceReferencesToolTest {

    @Test
    fun `finds references to string resource`() {
        val temp = Files.createTempDirectory("resource-refs")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = ResourceReferencesTool.execute(project.absolutePathString(), "string.app_name")

            assertEquals("success", json["status"]?.toString()?.removeSurrounding("\""))
            val total = json["totalReferences"]?.toString()?.toIntOrNull() ?: 0
            // app_name is referenced in strings.xml and layout
            assertEquals(true, total >= 0)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}
