package dev.normansanchez.androidmcp.flow

import dev.normansanchez.androidmcp.fixtures.FixtureProjects
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ModuleDiscoveryTest {

    @Test
    fun `multi-module project discovers modules types and dependencies`() {
        val temp = Files.createTempDirectory("modules-multi")
        try {
            val project = FixtureProjects.sampleAndroidProject(temp)

            val scan = AndroidProjectScanner.scan(project)

            assertNotNull(scan)
            assertEquals(setOf("app", "feature-login", "core-data"), scan.modules.map { it.name }.toSet())

            val app = scan.modules.first { it.name == "app" }
            assertEquals(AndroidProjectScanner.TYPE_APPLICATION, app.type)
            assertEquals(listOf(AndroidProjectScanner.TYPE_LIBRARY),
                scan.modules.filter { it.name != "app" }.map { it.type }.distinct())

            assertEquals(listOf("app"), scan.appModules.map { it.name })

            val pairs = scan.moduleDependencies.map { it.from to it.to }.toSet()
            assertTrue("app" to "core-data" in pairs)
            assertTrue("app" to "feature-login" in pairs)
            assertTrue("feature-login" to "core-data" in pairs)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `single-module project is discovered from settings includes`() {
        val temp = Files.createTempDirectory("modules-single")
        try {
            val project = FixtureProjects.xmlFragmentApp(temp)

            val scan = AndroidProjectScanner.scan(project)

            assertNotNull(scan)
            assertEquals(listOf("app"), scan.modules.map { it.name })
            assertEquals(AndroidProjectScanner.TYPE_APPLICATION, scan.appModules.single().type)
            assertEquals("app", scan.appModules.single().name)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `non-project directory yields no scan`() {
        val temp = Files.createTempDirectory("modules-empty")
        try {
            assertNull(AndroidProjectScanner.scan(temp))
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}