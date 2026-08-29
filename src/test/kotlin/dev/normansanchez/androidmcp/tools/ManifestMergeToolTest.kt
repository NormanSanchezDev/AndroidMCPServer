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
    fun `reports module origin for each conflicting attribute value`() {
        val temp = Files.createTempDirectory("manifest-conflict")
        try {
            val appManifest = """
                <?xml version="1.0" encoding="utf-8"?>
                <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                    <application>
                        <activity android:name="com.acme.AuthActivity" android:exported="true" />
                    </application>
                </manifest>
            """.trimIndent()
            val coreManifest = """
                <?xml version="1.0" encoding="utf-8"?>
                <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                    <application>
                        <activity android:name="com.acme.AuthActivity" android:exported="false" />
                    </application>
                </manifest>
            """.trimIndent()
            writeManifest(temp.resolve("app"), appManifest)
            writeManifest(temp.resolve("core"), coreManifest)
            Files.writeString(temp.resolve("settings.gradle.kts"), "rootProject.name = \"conflict\"\n")

            val json = ManifestMergeTool.execute(temp.absolutePathString())

            assertEquals("success", json["status"]?.toString()?.removeSurrounding("\""))
            assertEquals(true, (json["conflictCount"]?.toString()?.toIntOrNull() ?: 0) >= 1)

            val conflict = json["conflicts"]!!.jsonArray
                .map { it.jsonObject }
                .first { it["attribute"]!!.jsonPrimitive.content == "exported" }
            assertEquals("activity", conflict["type"]!!.jsonPrimitive.content)
            assertEquals("com.acme.AuthActivity", conflict["component"]!!.jsonPrimitive.content)

            val valuesByModule = conflict["values"]!!.jsonArray
                .map { it.jsonObject }
                .associate {
                    it["module"]!!.jsonPrimitive.content to it["value"]!!.jsonPrimitive.content
                }
            assertEquals(mapOf("app" to "true", "core" to "false"), valuesByModule)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    private fun writeManifest(moduleDir: java.nio.file.Path, content: String) {
        val dir = moduleDir.resolve("src/main")
        Files.createDirectories(dir)
        Files.writeString(dir.resolve("AndroidManifest.xml"), content)
        Files.writeString(moduleDir.resolve("build.gradle.kts"), "")
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
