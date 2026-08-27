package dev.normansanchez.androidmcp.tools

import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SymbolHierarchyToolTest {

    @Test
    fun `builds hierarchy for UserRepository`() {
        val temp = Files.createTempDirectory("symbol-hierarchy")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = SymbolHierarchyTool.execute(project.absolutePathString(), "UserRepository")

            assertEquals("success", json["status"]?.toString()?.removeSurrounding("\""))
            val hierarchy = json["hierarchy"]?.toString() ?: ""
            assertTrue(hierarchy.contains("UserRepository"))
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `returns not_found for missing class`() {
        val temp = Files.createTempDirectory("symbol-hierarchy-missing")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = SymbolHierarchyTool.execute(project.absolutePathString(), "GhostClass")

            assertEquals("not_found", json["status"]?.toString()?.removeSurrounding("\""))
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}
