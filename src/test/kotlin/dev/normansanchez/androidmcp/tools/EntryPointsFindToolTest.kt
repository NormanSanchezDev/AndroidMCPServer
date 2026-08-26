package dev.normansanchez.androidmcp.tools

import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EntryPointsFindToolTest {

    @Test
    fun `finds launcher activity deep links and exported components`() {
        val temp = Files.createTempDirectory("entry-points")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = EntryPointsFindTool.execute(project.absolutePathString(), "app")

            assertEquals("success", json["status"]!!.jsonPrimitive.content)

            val launchers = json["launchers"]!!.jsonArray.map { it.jsonObject }
            assertEquals(1, launchers.size)
            assertEquals(".MainActivity", launchers[0]["component"]!!.jsonPrimitive.content)

            val deepLinks = json["deepLinks"]!!.jsonArray.map { it.jsonObject }
            assertEquals(1, deepLinks.size)
            assertEquals(".MainActivity", deepLinks[0]["component"]!!.jsonPrimitive.content)
            assertTrue(
                "corporate" in deepLinks[0]["schemes"]!!.jsonArray.map { it.jsonPrimitive.content }
            )
            assertEquals(false, deepLinks[0]["autoVerify"]!!.jsonPrimitive.boolean)

            assertEquals(2, json["exportedComponentCount"]!!.jsonPrimitive.int)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `returns not_available for module without manifest`() {
        val temp = Files.createTempDirectory("entry-points-empty")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.GradleFixtureProject.create(temp)

            val json = EntryPointsFindTool.execute(project.absolutePathString(), "app")

            assertEquals("not_available", json["status"]!!.jsonPrimitive.content)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}
