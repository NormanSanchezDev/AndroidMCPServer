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

class ModuleGraphToolTest {

    @Test
    fun `builds module graph from fixture project`() {
        val temp = Files.createTempDirectory("module-graph")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = ModuleGraphTool.execute(project.absolutePathString())

            assertEquals("success", json["status"]!!.jsonPrimitive.content)
            assertEquals(3, json["moduleCount"]!!.jsonPrimitive.int)

            val edges = json["edges"]!!.jsonArray.map { it.jsonObject }
            assertEquals(3, edges.size)

            val edgePairs = edges.map { it["from"]!!.jsonPrimitive.content to it["to"]!!.jsonPrimitive.content }.toSet()
            assertTrue("app" to "core-data" in edgePairs)
            assertTrue("app" to "feature-login" in edgePairs)
            assertTrue("feature-login" to "core-data" in edgePairs)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `returns not_available without settings file`() {
        val temp = Files.createTempDirectory("no-settings")
        try {
            val json = ModuleGraphTool.execute(temp.absolutePathString())

            assertEquals("not_available", json["status"]!!.jsonPrimitive.content)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}
