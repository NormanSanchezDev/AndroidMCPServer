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

class SymbolFindToolTest {

    @Test
    fun `finds class declaration by name in fixture sources`() {
        val temp = Files.createTempDirectory("symbol-find")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = SymbolFindTool.execute(
                projectRoot = project.absolutePathString(),
                query = "UserRepository",
                exactMatch = true
            )

            assertEquals("success", json["status"]!!.jsonPrimitive.content)
            assertTrue(json["scannedFileCount"]!!.jsonPrimitive.int >= 5)

            val matches = json["matches"]!!.jsonArray.map { it.jsonObject }
            assertEquals(1, matches.size)

            val repositoryClass = matches.first()
            assertEquals("class", repositoryClass["kind"]!!.jsonPrimitive.content)
            assertEquals("UserRepository", repositoryClass["name"]!!.jsonPrimitive.content)
            assertEquals(
                "com.corporate.data.UserRepository",
                repositoryClass["fqName"]!!.jsonPrimitive.content
            )
            assertEquals(
                "app/src/main/java/com/corporate/data/UserRepository.kt",
                repositoryClass["file"]!!.jsonPrimitive.content
            )
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `finds constructor and member properties with partial match`() {
        val temp = Files.createTempDirectory("symbol-find-partial")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = SymbolFindTool.execute(
                projectRoot = project.absolutePathString(),
                query = "userRepository"
            )

            val names = json["matches"]!!.jsonArray.map { it.jsonObject["name"]!!.jsonPrimitive.content }
            assertTrue(names.contains("userRepository"))
            assertTrue(names.contains("UserRepository"))
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `filters by kind`() {
        val temp = Files.createTempDirectory("symbol-find-kind")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = SymbolFindTool.execute(
                projectRoot = project.absolutePathString(),
                query = "user",
                kind = "function"
            )

            val matches = json["matches"]!!.jsonArray.map { it.jsonObject }
            assertTrue(matches.isNotEmpty() && matches.all { it["kind"]!!.jsonPrimitive.content == "function" })
            assertTrue(matches.any { it["name"]!!.jsonPrimitive.content == "currentUser" })
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `returns zero matches without inventing results`() {
        val temp = Files.createTempDirectory("symbol-none")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = SymbolFindTool.execute(
                projectRoot = project.absolutePathString(),
                query = "DoesNotExistAnywhere"
            )

            assertEquals(0, json["matchCount"]!!.jsonPrimitive.int)
            assertEquals(0, json["matches"]!!.jsonArray.size)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}
