package dev.normansanchez.androidmcp.graph

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModuleGraphParserTest {

    @Test
    fun `extracts root name and modules from kotlin dsl settings`() {
        val settings = """
            pluginManagement {
                repositories {
                    google()
                }
            }

            rootProject.name = "CorporateApp"

            include(":app")
            include(":feature-login", ":core-data")
            include(":core-design")
        """.trimIndent()

        val (rootName, modules) = ModuleGraphParser.parseSettings(settings)

        assertEquals("CorporateApp", rootName)
        assertEquals(listOf("app", "feature-login", "core-data", "core-design"), modules)
    }

    @Test
    fun `extracts modules from groovy settings`() {
        val settings = """
            rootProject.name = 'LegacyApp'
            include ':app'
            include ':lib'
        """.trimIndent()

        val (rootName, modules) = ModuleGraphParser.parseSettings(settings)

        assertEquals("LegacyApp", rootName)
        assertEquals(listOf("app", "lib"), modules)
    }

    @Test
    fun `extracts edges with configuration from build file`() {
        val buildFile = """
            plugins {
                id("com.android.application")
            }

            dependencies {
                implementation(project(":core-data"))
                api(project(":core-design"))
                testImplementation(project(":core-testing"))
                implementation(libs.kotlin.stdlib)
                implementation("com.squareup.okhttp3:okhttp:4.12.0")
            }
        """.trimIndent() + "\n"

        val edges = ModuleGraphParser.parseBuildFile("app", buildFile)

        assertEquals(3, edges.size)
        assertTrue(edges.contains(ModuleEdge("app", "core-data", "implementation")))
        assertTrue(edges.contains(ModuleEdge("app", "core-design", "api")))
        assertTrue(edges.contains(ModuleEdge("app", "core-testing", "testImplementation")))
    }
}
