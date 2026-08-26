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

class SymbolReferencesToolTest {

    @Test
    fun `separates declaration from references for UserRepository`() {
        val temp = Files.createTempDirectory("symbol-refs")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = SymbolReferencesTool.execute(
                projectRoot = project.absolutePathString(),
                symbolName = "UserRepository"
            )

            assertEquals("success", json["status"]!!.jsonPrimitive.content)
            assertEquals(1, json["declarationCount"]!!.jsonPrimitive.int)

            val declaration = json["declarations"]!!.jsonArray.first().jsonObject
            assertEquals(
                "app/src/main/java/com/corporate/data/UserRepository.kt",
                declaration["file"]!!.jsonPrimitive.content
            )
            assertTrue(declaration["line"]!!.jsonPrimitive.int > 0)

            val references = json["references"]!!.jsonArray.map { it.jsonObject }
            assertTrue(references.size >= 3)

            val files = references.map { it["file"]!!.jsonPrimitive.content }.toSet()
            assertTrue(
                "app/src/main/java/com/corporate/app/MainActivity.kt" in files,
                "usage inside MainActivity must be reported"
            )
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `reports zero occurrences honestly when symbol absent`() {
        val temp = Files.createTempDirectory("symbol-refs-none")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = SymbolReferencesTool.execute(
                projectRoot = project.absolutePathString(),
                symbolName = "GhostSymbol"
            )

            assertEquals(0, json["declarationCount"]!!.jsonPrimitive.int)
            assertEquals(0, json["references"]!!.jsonArray.size)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `rejects non identifier input`() {
        val temp = Files.createTempDirectory("symbol-refs-invalid")
        try {
            val project = dev.normansanchez.androidmcp.fixtures.FixtureProjects.sampleAndroidProject(temp)

            val json = SymbolReferencesTool.execute(
                projectRoot = project.absolutePathString(),
                symbolName = "not a valid;identifier"
            )

            assertEquals("invalid_request", json["status"]!!.jsonPrimitive.content)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}
