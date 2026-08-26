package dev.normansanchez.androidmcp.tools

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProjectInspectToolTest {

    @Test
    fun `detects gradle project with android modules and evidence`() {
        val temp = Files.createTempDirectory("inspect")
        try {
            val root = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = ProjectInspectTool.execute(root.absolutePathString())

            assertEquals("success", json["status"]!!.jsonPrimitive.content)
            assertTrue((json["gradleProject"] as JsonPrimitive).boolean)
            assertTrue(json["settingsFile"]!!.jsonPrimitive.content.endsWith("settings.gradle.kts"))
            assertTrue((json["androidProject"] as JsonPrimitive).boolean)

            val modules = json["modules"]!!.jsonArray
            assertEquals(3, modules.size)

            val typesByPath = modules.associate {
                it.jsonObject["path"]!!.jsonPrimitive.content to it.jsonObject["type"]!!.jsonPrimitive.content
            }
            assertEquals("application", typesByPath["app"])
            assertEquals("library", typesByPath["core-data"])
            assertEquals("library", typesByPath["feature-login"])

            val app = modules.first { it.jsonObject["path"]!!.jsonPrimitive.content == "app" }.jsonObject
            assertEquals("app/build.gradle.kts", app["buildFile"]!!.jsonPrimitive.content)
            assertEquals("app/src/main/AndroidManifest.xml", app["manifest"]!!.jsonPrimitive.content)

            val evidenceTypes = json["evidence"]!!.jsonArray.map { it.jsonObject["type"]!!.jsonPrimitive.content }.toSet()
            assertTrue("android_gradle_plugin" in evidenceTypes)
            assertTrue("android_manifest" in evidenceTypes)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `returns invalid_project for missing path`() {
        val result = ProjectInspectTool.execute("/definitely/not/a/project")

        assertEquals("invalid_project", result["status"]!!.jsonPrimitive.content)
        assertFalse((result["exists"] as JsonPrimitive).boolean)
    }

    @Test
    fun `reports empty module list for non gradle directory`() {
        val temp = Files.createTempDirectory("empty-project")
        try {
            val result = ProjectInspectTool.execute(temp.absolutePathString())

            assertEquals("success", result["status"]!!.jsonPrimitive.content)
            assertEquals(0, result["modules"]!!.jsonArray.size)
            assertFalse((result["androidProject"] as JsonPrimitive).boolean)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}
