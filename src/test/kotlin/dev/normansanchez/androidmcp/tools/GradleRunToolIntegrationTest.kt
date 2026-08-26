package dev.normansanchez.androidmcp.tools

import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GradleRunToolIntegrationTest {

    @Test
    fun `executes help task on a real gradle project`() {
        val temp = Files.createTempDirectory("gradle-run")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.GradleFixtureProject.create(temp)

            val json = GradleRunTool.execute(
                projectRoot = project.absolutePathString(),
                tasks = listOf("help"),
                flags = listOf("--console=plain"),
                timeoutSeconds = 240
            )

            assertEquals("executed", json["status"]!!.jsonPrimitive.content)
            assertEquals(true, json["success"]!!.jsonPrimitive.boolean)
            assertTrue(json["stdout"]!!.jsonPrimitive.content.contains("BUILD SUCCESSFUL"))
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `rejects invalid task names without executing gradle`() {
        val json = GradleRunTool.execute(
            projectRoot = Files.createTempDirectory("x").absolutePathString(),
            tasks = listOf("clean; rm -rf /")
        )

        assertEquals("invalid_task", json["status"]!!.jsonPrimitive.content)
    }

    @Test
    fun `rejects flags outside allowlist`() {
        val json = GradleRunTool.execute(
            projectRoot = Files.createTempDirectory("x").absolutePathString(),
            tasks = listOf("help"),
            flags = listOf("--init-script")
        )

        assertEquals("invalid_flag", json["status"]!!.jsonPrimitive.content)
    }
}
