package dev.normansanchez.androidmcp.lint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class LintXmlParserTest {

    @Test
    fun `parses issues with severity and location`() {
        val xml = resource("fixtures/lint/lint-results-debug.xml")

        val report = LintXmlParser.parse(xml)

        assertNotNull(report)
        assertEquals("6", report.format)
        assertEquals("lint 8.7.0", report.lintVersion)
        assertEquals(3, report.issues.size)

        assertEquals(1, report.countBySeverity("Error"))
        assertEquals(2, report.countBySeverity("Warning"))

        val hardcoded = report.issues.first { it.id == "HardcodedText" }
        assertEquals("Error", hardcoded.severity)
        assertEquals("src/main/res/layout/activity_main.xml", hardcoded.file)
        assertEquals(17, hardcoded.line)
        assertEquals("Internationalization", hardcoded.category)

        val icon = report.issues.first { it.id == "MissingApplicationIcon" }
        assertEquals(6, icon.line)
    }

    private fun resource(path: String): String =
        LintXmlParserTest::class.java.classLoader.getResource(path)!!.readText()
}
