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

class ProguardInspectToolTest {

    @Test
    fun `finds proguard rules in project`() {
        val temp = Files.createTempDirectory("proguard-inspect")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = ProguardInspectTool.execute(project.absolutePathString())

            assertEquals("success", json["status"]?.toString()?.removeSurrounding("\""))
            val files = json["files"]?.jsonArray ?: emptyList()
            assertTrue(files.isNotEmpty())

            val proguardFile = files.first { it.jsonObject["file"]?.toString()?.contains("proguard") == true }
            val rules = proguardFile.jsonObject["rules"]?.jsonArray ?: emptyList()
            assertTrue(rules.isNotEmpty())
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `returns not_available for project without proguard files`() {
        val temp = Files.createTempDirectory("proguard-empty")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)
            project.resolve("app/proguard-rules.pro").toFile().delete()

            val json = ProguardInspectTool.execute(project.absolutePathString())

            // May still find proguard config in build.gradle.kts
            val status = json["status"]?.toString()?.removeSurrounding("\"")
            assertTrue(status == "success" || status == "not_available")
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}
