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

    @Test
    fun `excludes build-logic and buildSrc directories from modules`() {
        val temp = Files.createTempDirectory("inspect-infra")
        try {
            Files.writeString(temp.resolve("settings.gradle.kts"), "rootProject.name = \"infra\"\n")
            val appDir = temp.resolve("app")
            Files.createDirectories(appDir.resolve("src/main"))
            Files.writeString(appDir.resolve("build.gradle.kts"), """
                plugins {
                    alias(libs.plugins.android.application)
                }
            """.trimIndent())
            Files.writeString(appDir.resolve("src/main/AndroidManifest.xml"), "<manifest/>")
            Files.createDirectories(temp.resolve("build-logic"))
            Files.writeString(temp.resolve("build-logic/build.gradle.kts"), "plugins { alias(libs.plugins.android.application) }")
            Files.createDirectories(temp.resolve("buildSrc"))
            Files.writeString(temp.resolve("buildSrc/build.gradle.kts"), "plugins { `kotlin-dsl` }")

            val result = ProjectInspectTool.execute(temp.absolutePathString())

            val modules = result["modules"]!!.jsonArray
            assertEquals(1, modules.size)
            assertEquals("app", modules.first().jsonObject["path"]!!.jsonPrimitive.content)
            assertEquals("application", modules.first().jsonObject["type"]!!.jsonPrimitive.content)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `reports root single module with its own build file`() {
        val temp = Files.createTempDirectory("inspect-root")
        try {
            Files.writeString(temp.resolve("settings.gradle.kts"), "rootProject.name = \"root\"\n")
            Files.writeString(temp.resolve("build.gradle.kts"), """
                plugins {
                    id("com.android.application")
                }
            """.trimIndent())
            Files.createDirectories(temp.resolve("src/main"))
            Files.writeString(temp.resolve("src/main/AndroidManifest.xml"), "<manifest/>")

            val result = ProjectInspectTool.execute(temp.absolutePathString())

            val modules = result["modules"]!!.jsonArray
            assertEquals(1, modules.size)
            assertEquals(".", modules.first().jsonObject["path"]!!.jsonPrimitive.content)
            assertEquals("application", modules.first().jsonObject["type"]!!.jsonPrimitive.content)
            assertTrue(modules.first().jsonObject["buildFile"]!!.jsonPrimitive.content.endsWith("build.gradle.kts"))
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `ignores root build file that only declares plugins with apply false`() {
        val temp = Files.createTempDirectory("inspect-root-false")
        try {
            Files.writeString(temp.resolve("settings.gradle.kts"), "rootProject.name = \"root\"\n")
            Files.writeString(temp.resolve("build.gradle.kts"), """
                plugins {
                    id("com.android.application") apply false
                    alias(libs.plugins.kotlin.android) apply false
                }
            """.trimIndent())

            val result = ProjectInspectTool.execute(temp.absolutePathString())

            assertEquals(0, result["modules"]!!.jsonArray.size)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}
