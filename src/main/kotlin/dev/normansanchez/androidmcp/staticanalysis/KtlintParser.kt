package dev.normansanchez.androidmcp.staticanalysis

data class KtlintIssue(
    val rule: String,
    val file: String,
    val line: Int,
    val column: Int,
    val message: String,
    val severity: String
)

object KtlintParser {

    fun parse(textOutput: String): List<KtlintIssue> {
        val issues = mutableListOf<KtlintIssue>()

        val pattern = Regex("""(.+?):(\d+):(\d+): (.+) \[(.+)\]""")

        for (line in textOutput.lines()) {
            val match = pattern.find(line.trim()) ?: continue
            issues.add(
                KtlintIssue(
                    file = match.groupValues[1],
                    line = match.groupValues[2].toIntOrNull() ?: 0,
                    column = match.groupValues[3].toIntOrNull() ?: 0,
                    message = match.groupValues[4],
                    rule = match.groupValues[5],
                    severity = "warning"
                )
            )
        }

        return issues
    }
}
