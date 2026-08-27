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

class ManifestMergeToolTest {

    @Test
    fun `finds manifests across modules`() {
        val temp = Files.createTempDirectory("manifest-merge")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = ManifestMergeTool.execute(project.absolutePathString())

            assertEquals("success", json["status"]?.toString()?.removeSurrounding("\""))
            assertEquals(true, (json["manifestCount"]?.toString()?.toIntOrNull() ?: 0) >= 3)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `returns not_available for project without manifests`() {
        val temp = Files.createTempDirectory("manifest-merge-empty")
        try {
            Files.createDirectories(temp.resolve("project"))
            val json = ManifestMergeTool.execute(temp.resolve("project").absolutePathString())

            assertEquals("not_available", json["status"]?.toString()?.removeSurrounding("\""))
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}
