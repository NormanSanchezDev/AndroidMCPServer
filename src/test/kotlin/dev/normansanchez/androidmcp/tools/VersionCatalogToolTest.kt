package dev.normansanchez.androidmcp.tools

import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VersionCatalogToolTest {

    @Test
    fun `parses version catalog`() {
        val temp = Files.createTempDirectory("version-catalog")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = VersionCatalogTool.execute(project.absolutePathString())

            assertEquals("success", json["status"]?.toString()?.removeSurrounding("\""))
            val libraries = json["libraries"]?.toString() ?: ""
            assertTrue(libraries.contains("core-ktx"))
            assertTrue(libraries.contains("androidx.core"))
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `returns not_available when no catalog`() {
        val temp = Files.createTempDirectory("no-catalog")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)
            val catalogFile = project.resolve("gradle/libs.versions.toml")
            Files.deleteIfExists(catalogFile)

            val json = VersionCatalogTool.execute(project.absolutePathString())

            assertEquals("not_available", json["status"]?.toString()?.removeSurrounding("\""))
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}
