package dev.normansanchez.androidmcp.tools

import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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

    @Test
    fun `ignores generated manifests under build directories`() {
        val temp = Files.createTempDirectory("security-build")
        try {
            writeManifest(temp.resolve("app"), """android:allowBackup="true"""")
            writeManifest(temp.resolve("build/intermediates/merged_manifests/debug"), """android:allowBackup="true"""")

            val json = SecurityAuditTool.execute(temp.absolutePathString())

            val backupIssues = json["issues"]!!.jsonArray
                .map { it.jsonObject }
                .filter { it["category"]!!.jsonPrimitive.content == "backup" }
            assertEquals(1, backupIssues.size, "merged manifest under build/ must be excluded")
            assertTrue(backupIssues.first()["file"]!!.jsonPrimitive.content.startsWith("app/"))
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `only flags hardcoded secrets with a closing quote`() {
        val temp = Files.createTempDirectory("security-secrets")
        try {
            val srcDir = temp.resolve("app/src/main/java/com/acme")
            Files.createDirectories(srcDir)
            Files.writeString(
                srcDir.resolve("Secrets.kt"),
                """
                package com.acme

                object Secrets {
                    val closed = password = "Y2hhbmdlLW1lLmFhYWFhYQ=="
                    val unterminated = token = "Y2hhbmdlLW1lLmFhYWFhYQ==
                    val tooShort = api_key = "abc"
                }
                """.trimIndent()
            )

            val json = SecurityAuditTool.execute(temp.absolutePathString())

            val secretIssues = json["issues"]!!.jsonArray
                .map { it.jsonObject }
                .filter { it["category"]!!.jsonPrimitive.content == "hardcoded_secret" }
            assertEquals(1, secretIssues.size, "only the closed-quote value is a secret")
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    private fun writeManifest(moduleDir: java.nio.file.Path, body: String) {
        val dir = moduleDir.resolve("src/main")
        Files.createDirectories(dir)
        Files.writeString(
            dir.resolve("AndroidManifest.xml"),
            """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                <application $body />
            </manifest>
            """.trimIndent()
        )
    }
}
