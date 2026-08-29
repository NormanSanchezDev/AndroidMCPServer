package dev.normansanchez.androidmcp.flow

import dev.normansanchez.androidmcp.fixtures.FixtureProjects
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EntryPointDetectorTest {

    @Test
    fun `xml fixture exposes splash launcher as declared entry point`() {
        val temp = Files.createTempDirectory("entry-xml")
        try {
            val project = FixtureProjects.xmlFragmentApp(temp)
            val scan = AndroidProjectScanner.scan(project)!!

            val entries = EntryPointDetector.detect(scan)

            assertEquals(1, entries.size)
            val splash = entries.single()
            assertEquals("com.acme.shop.ui.SplashActivity", splash.component)
            assertEquals("activity", splash.kind)
            assertEquals("app", splash.module)
            assertTrue(splash.isSplash)
            assertEquals(EvidenceType.DECLARED, splash.evidence.evidenceType)
            assertEquals(1.0, splash.evidence.confidence)
            assertTrue(splash.evidence.file.endsWith("AndroidManifest.xml"))
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `alias target is resolved and multiple launchers reported`() {
        val temp = Files.createTempDirectory("entry-ambiguous")
        try {
            val project = FixtureProjects.ambiguousEntryApp(temp)
            val scan = AndroidProjectScanner.scan(project)!!

            val entries = EntryPointDetector.detect(scan)

            assertEquals(3, entries.size)
            val alias = entries.first { it.kind == "activity_alias" }
            assertEquals("com.acme.launch.alias.SplashAlias", alias.component)
            assertEquals("com.acme.launch.ui.SplashActivity", alias.targetComponent)
            assertTrue(alias.isSplash)

            val declaredActivities = EntryPointDetector.declaredActivities(scan)
            assertEquals(setOf("SplashActivity", "MainActivity"), declaredActivities.map { it.simpleName }.toSet())
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `compose fixture single launcher resolves`() {
        val temp = Files.createTempDirectory("entry-compose")
        try {
            val project = FixtureProjects.composeApp(temp)
            val scan = AndroidProjectScanner.scan(project)!!

            val entries = EntryPointDetector.detect(scan)

            assertEquals(1, entries.size)
            assertEquals("com.acme.auth.MainActivity", entries.single().component)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}