package dev.normansanchez.androidmcp.staticanalysis

data class DetektIssue(
    val rule: String,
    val severity: String,
    val message: String,
    val file: String,
    val line: Int,
    val column: Int
)

object DetektParser {

    fun parse(xmlContent: String): List<DetektIssue> {
        val issues = mutableListOf<DetektIssue>()

        val findingPattern = Regex(
            """<file\b[^>]*name="([^"]+)"[^>]*>.*?<finding\b[^>]*severity="([^"]+)"[^>]*rule="([^"]+)"[^>]*line="(\d+)"[^>]*column="(\d+)"[^>]*>(.*?)</finding>""",
            RegexOption.DOT_MATCHES_ALL
        )

        findingPattern.findAll(xmlContent).forEach { match ->
            issues.add(
                DetektIssue(
                    rule = match.groupValues[3],
                    severity = match.groupValues[2],
                    message = match.groupValues[6].trim(),
                    file = match.groupValues[1],
                    line = match.groupValues[4].toIntOrNull() ?: 0,
                    column = match.groupValues[5].toIntOrNull() ?: 0
                )
            )
        }

        if (issues.isEmpty()) {
            val simplePattern = Regex("""<error\b[^>]*>(.*?)</error>""", RegexOption.DOT_MATCHES_ALL)
            val fileAttr = Regex("""name="([^"]+)"""")
            val severityAttr = Regex("""severity="([^"]+)"""")
            val lineAttr = Regex("""line="(\d+)"""")

            simplePattern.findAll(xmlContent).forEach { match ->
                val errorXml = match.groupValues[1]
                val file = fileAttr.find(errorXml)?.groupValues?.get(1) ?: "unknown"
                val severity = severityAttr.find(errorXml)?.groupValues?.get(1) ?: "warning"
                val line = lineAttr.find(errorXml)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val message = errorXml.substringAfter(">").substringBefore("</").trim()

                issues.add(
                    DetektIssue(
                        rule = "detekt",
                        severity = severity,
                        message = message,
                        file = file,
                        line = line,
                        column = 0
                    )
                )
            }
        }

        return issues
    }
}
