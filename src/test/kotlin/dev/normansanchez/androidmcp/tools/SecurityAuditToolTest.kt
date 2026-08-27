package dev.normansanchez.androidmcp.tools

import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SecurityAuditToolTest {

    @Test
    fun `runs audit on sample project`() {
        val temp = Files.createTempDirectory("security-audit")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = SecurityAuditTool.execute(project.absolutePathString())

            assertEquals("success", json["status"]?.toString()?.removeSurrounding("\""))
            val total = json["totalIssues"]?.toString()?.toIntOrNull() ?: 0
            assertTrue(total >= 0)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}
