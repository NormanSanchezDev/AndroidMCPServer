package dev.normansanchez.androidmcp.tools

import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GradleTasksToolIntegrationTest {

    @Test
    fun `lists tasks from a real gradle project`() {
        val temp = Files.createTempDirectory("gradle-tasks")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.GradleFixtureProject.create(temp)

            val json = GradleTasksTool.execute(project.absolutePathString())

            assertEquals("success", json["status"]!!.jsonPrimitive.content)

            val names = json["tasks"]!!.jsonArray.map { it.jsonObject["name"]!!.jsonPrimitive.content }
            assertTrue("help" in names)
            assertTrue("tasks" in names)
            assertTrue("init" in names)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `reports gradle_not_available without wrapper`() {
        val temp = Files.createTempDirectory("no-wrapper")
        try {
            val json = GradleTasksTool.execute(temp.absolutePathString())

            assertEquals("gradle_not_available", json["status"]!!.jsonPrimitive.content)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `reports invalid_project for missing root`() {
        val json = GradleTasksTool.execute("/definitely/not/a/project")

        assertEquals("invalid_project", json["status"]!!.jsonPrimitive.content)
    }

    @Test
    fun `reports invalid_module before looking for a wrapper`() {
        val temp = Files.createTempDirectory("invalid-module")
        try {
            val json = GradleTasksTool.execute(temp.absolutePathString(), module = "a b")

            assertEquals("invalid_module", json["status"]!!.jsonPrimitive.content)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}
