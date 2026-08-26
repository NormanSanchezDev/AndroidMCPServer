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

class ResourcesInspectToolTest {

    @Test
    fun `enumerates resource folders and value names`() {
        val temp = Files.createTempDirectory("resources")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = ResourcesInspectTool.execute(project.absolutePathString(), "app")

            assertEquals("success", json["status"]!!.jsonPrimitive.content)
            assertEquals(3, json["folderCount"]!!.jsonPrimitive.int)

            val folders = json["folders"]!!.jsonArray.map { it.jsonObject }
            val drawable = folders.first { it["name"]!!.jsonPrimitive.content == "drawable-xxhdpi" }
            assertEquals("xxhdpi", drawable["qualifier"]!!.jsonPrimitive.content)
            assertEquals(1, drawable["fileCount"]!!.jsonPrimitive.int)

            assertEquals(4, json["fileCount"]!!.jsonPrimitive.int)

            val valueItems = json["valueItems"]!!.jsonArray.map { it.jsonObject }
            val strings = valueItems.first { it["tag"]!!.jsonPrimitive.content == "string" }
            val names = strings["names"]!!.jsonArray.map { it.jsonPrimitive.content }
            assertTrue("app_name" in names)
            assertTrue("unused_title" in names)

            val colors = valueItems.first { it["tag"]!!.jsonPrimitive.content == "color" }
            assertTrue("brand_primary" in colors["names"]!!.jsonArray.map { it.jsonPrimitive.content })
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `returns not_available for module without res directory`() {
        val temp = Files.createTempDirectory("resources-empty")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.GradleFixtureProject.create(temp)

            val json = ResourcesInspectTool.execute(project.absolutePathString(), "app")

            assertEquals("not_available", json["status"]!!.jsonPrimitive.content)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}
