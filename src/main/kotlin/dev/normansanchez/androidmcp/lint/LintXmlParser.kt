package dev.normansanchez.androidmcp.lint

data class LintIssue(
    val id: String,
    val severity: String,
    val message: String,
    val category: String?,
    val priority: Int?,
    val file: String?,
    val line: Int?
)

data class LintReport(
    val format: String,
    val lintVersion: String?,
    val issues: List<LintIssue>
) {
    fun countBySeverity(severity: String): Int =
        issues.count { it.severity.equals(severity, ignoreCase = true) }
}

object LintXmlParser {

    fun parse(xmlContent: String): LintReport? {
        val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
        val document = try {
            factory.newDocumentBuilder()
                .parse(org.xml.sax.InputSource(java.io.StringReader(xmlContent)))
        } catch (_: Exception) {
            return null
        }

        val root = document.documentElement
        if (root.tagName != "issues") {
            return null
        }

        val issues = mutableListOf<LintIssue>()
        val issueNodes = root.getElementsByTagName("issue")

        for (index in 0 until issueNodes.length) {
            val element = issueNodes.item(index) as? org.w3c.dom.Element ?: continue

            val location = element.getElementsByTagName("location")
                .item(0) as? org.w3c.dom.Element

            issues.add(
                LintIssue(
                    id = element.getAttribute("id"),
                    severity = element.getAttribute("severity"),
                    message = element.getAttribute("message"),
                    category = element.getAttribute("category").takeIf { it.isNotBlank() },
                    priority = element.getAttribute("priority").toIntOrNull(),
                    file = location?.getAttribute("file")?.takeIf { it.isNotBlank() },
                    line = location?.getAttribute("line")?.toIntOrNull()?.takeIf { it > 0 }
                )
            )
        }

        return LintReport(
            format = root.getAttribute("format"),
            lintVersion = root.getAttribute("by").takeIf { it.isNotBlank() },
            issues = issues
        )
    }
}
