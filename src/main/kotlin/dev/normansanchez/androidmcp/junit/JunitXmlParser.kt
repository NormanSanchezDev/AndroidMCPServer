package dev.normansanchez.androidmcp.junit

data class JunitTestCase(
    val name: String,
    val classname: String,
    val timeSeconds: Double?,
    val failureMessage: String?,
    val failureType: String?,
    val skippedMessage: String?
)

data class JunitTestSuite(
    val name: String,
    val tests: Int,
    val failures: Int,
    val errors: Int,
    val skipped: Int,
    val timeSeconds: Double?,
    val timestamp: String?,
    val testCases: List<JunitTestCase>
)

object JunitXmlParser {

    fun parse(xmlContent: String): JunitTestSuite? {
        val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
        val document = try {
            factory.newDocumentBuilder()
                .parse(org.xml.sax.InputSource(java.io.StringReader(xmlContent)))
        } catch (_: Exception) {
            return null
        }

        val suite = document.documentElement
        if (suite.tagName != "testsuite") {
            return null
        }

        val testCases = mutableListOf<JunitTestCase>()
        val caseNodes = suite.getElementsByTagName("testcase")

        for (index in 0 until caseNodes.length) {
            val element = caseNodes.item(index) as? org.w3c.dom.Element ?: continue

            testCases.add(
                JunitTestCase(
                    name = element.getAttribute("name"),
                    classname = element.getAttribute("classname"),
                    timeSeconds = element.getAttribute("time").toDoubleOrNull(),
                    failureMessage = firstChildText(element, "failure")?.second,
                    failureType = firstChildText(element, "failure")?.first,
                    skippedMessage = firstChildText(element, "skipped")?.second
                )
            )
        }

        return JunitTestSuite(
            name = suite.getAttribute("name"),
            tests = suite.getAttribute("tests").toIntOrNull() ?: testCases.size,
            failures = suite.getAttribute("failures").toIntOrNull() ?: 0,
            errors = suite.getAttribute("errors").toIntOrNull() ?: 0,
            skipped = suite.getAttribute("skipped").toIntOrNull() ?: 0,
            timeSeconds = suite.getAttribute("time").toDoubleOrNull(),
            timestamp = suite.getAttribute("timestamp").takeIf { it.isNotBlank() },
            testCases = testCases
        )
    }

    private fun firstChildText(
        parent: org.w3c.dom.Element,
        tagName: String
    ): Pair<String?, String?>? {
        val nodes = parent.getElementsByTagName(tagName)
        if (nodes.length == 0) {
            return null
        }
        val element = nodes.item(0) as? org.w3c.dom.Element ?: return null
        val type = element.getAttribute("type").takeIf { it.isNotBlank() }
        val message = element.getAttribute("message").takeIf { it.isNotBlank() }
        return type to message
    }
}
