package dev.normansanchez.androidmcp.tools

import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArchitectureDetectToolTest {

    @Test
    fun `detects hilt and compose`() {
        val temp = Files.createTempDirectory("arch-detect")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = ArchitectureDetectTool.execute(project.absolutePathString())

            assertEquals("success", json["status"]?.toString()?.removeSurrounding("\""))
            val diFramework = json["diFramework"]?.toString()?.removeSurrounding("\"")
            assertEquals("Hilt", diFramework)
            assertEquals(true, json["usesCompose"]?.toString()?.toBoolean())
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}
