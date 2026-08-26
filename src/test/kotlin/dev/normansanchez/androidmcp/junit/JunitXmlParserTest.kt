package dev.normansanchez.androidmcp.junit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class JunitXmlParserTest {

    @Test
    fun `parses passing suite`() {
        val xml = resource("fixtures/junit/TEST-com.corporate.app.ParserTest.xml")

        val suite = JunitXmlParser.parse(xml)

        assertNotNull(suite)
        assertEquals("com.corporate.app.ParserTest", suite.name)
        assertEquals(2, suite.tests)
        assertEquals(0, suite.failures)
        assertEquals(0, suite.errors)
        assertEquals(0, suite.skipped)
        assertEquals(2, suite.testCases.size)
        assertNull(suite.testCases[0].failureMessage)
    }

    @Test
    fun `parses failures and skipped with details`() {
        val xml = resource("fixtures/junit/TEST-com.corporate.data.RepositoryTest.xml")

        val suite = JunitXmlParser.parse(xml)

        assertNotNull(suite)
        assertEquals(3, suite.tests)
        assertEquals(1, suite.failures)
        assertEquals(1, suite.skipped)

        val failed = suite.testCases.first { it.failureMessage != null }
        assertEquals("propagates network failure()", failed.name)
        assertEquals("com.corporate.data.RepositoryTest", failed.classname)
        assertEquals(
            "expected: <IOException> but was: <NullPointerException>",
            failed.failureMessage
        )
        assertEquals("org.opentest4j.AssertionFailedError", failed.failureType)

        val skipped = suite.testCases.first { it.skippedMessage != null }
        assertEquals("refreshes stale cache()", skipped.name)
        assertEquals("flaky in CI", skipped.skippedMessage)
    }

    @Test
    fun `returns null for malformed xml`() {
        assertNull(JunitXmlParser.parse("<not-a-suite/>"))
        assertNull(JunitXmlParser.parse("garbage"))
    }

    private fun resource(path: String): String =
        JunitXmlParserTest::class.java.classLoader.getResource(path)!!.readText()
}
