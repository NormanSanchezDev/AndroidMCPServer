package dev.normansanchez.androidmcp.flow

data class DecisionOutcome(
    val label: String,
    val target: String?
)

data class Decision(
    val condition: String,
    val outcomes: List<DecisionOutcome>,
    val line: Int
)

object DecisionExtractor {

    fun decisions(content: String): List<Decision> {
        val decisions = mutableListOf<Decision>()
        decisions.addAll(extractWhen(content))
        decisions.addAll(extractIf(content))
        return decisions
    }

    private fun extractWhen(content: String): List<Decision> {
        val result = mutableListOf<Decision>()
        Regex("""\bwhen\s*\(\s*([^)]{0,120})\s*\)\s*\{""")
            .findAll(content)
            .forEach { match ->
                val expr = match.groupValues[1].trim()
                val blockEnd = KotlinNavExtractor.findClosingBrace(content, match.range.last)
                val block = content.substring(match.range.last, blockEnd)
                val outcomes = mutableListOf<DecisionOutcome>()
                Regex("""^\s*(is\s+[\w.$]+|[A-Za-z0-9_.$]+|else)\s*->\s*(.*)$""", RegexOption.MULTILINE)
                    .findAll(block)
                    .forEach { branch ->
                        val label = branch.groupValues[1].trim()
                        val rawBody = branch.groupValues[2].trim()
                        if (rawBody.isBlank()) return@forEach
                        val body = if (rawBody == "{") {
                            val openBrace = block.indexOf('{', branch.range.first)
                            val closeBrace = KotlinNavExtractor.findClosingBrace(block, openBrace)
                            block.substring(openBrace + 1, closeBrace)
                        } else {
                            rawBody
                        }
                        val targets = targetsIn(body)
                        outcomes.add(
                            DecisionOutcome(
                                label = label,
                                target = targets.firstOrNull()?.target
                            )
                        )
                    }
                if (outcomes.any { it.target != null }) {
                    result.add(
                        Decision(
                            condition = "when ($expr)",
                            outcomes = outcomes.filter { it.target != null },
                            line = SourceScanner.lineOf(content, match.range.first)
                        )
                    )
                }
            }
        return result
    }

    private fun extractIf(content: String): List<Decision> {
        val result = mutableListOf<Decision>()
        Regex("""\bif\s*\(\s*([^)]{1,160})\s*\)\s*\{""")
            .findAll(content)
            .forEach { match ->
                val condition = match.groupValues[1].trim()
                val thenEnd = KotlinNavExtractor.findClosingBrace(content, match.range.last)
                if (thenEnd - match.range.first > 4000) return@forEach
                val thenBlock = content.substring(match.range.last, thenEnd)

                val afterThen = content.substring(
                    (thenEnd + 1).coerceAtMost(content.length),
                    (thenEnd + 1 + 48).coerceAtMost(content.length)
                )
                val hasElse = Regex("""^\s*else\s*\{""").containsMatchIn(afterThen)
                val elseBlock = if (hasElse) {
                    val elseOpen = thenEnd + 1 + afterThen.indexOf("else")
                    val elseBrace = content.indexOf('{', elseOpen)
                    val elseEnd = KotlinNavExtractor.findClosingBrace(content, elseBrace)
                    content.substring(elseBrace, elseEnd)
                } else {
                    null
                }

                val thenTargets = targetsIn(thenBlock)
                val elseTargets = elseBlock?.let { targetsIn(it) } ?: emptyList()
                if (thenTargets.isNotEmpty() || elseTargets.isNotEmpty()) {
                    val outcomes = mutableListOf<DecisionOutcome>()
                    val firstThen = thenTargets.firstOrNull()
                    if (firstThen != null) {
                        outcomes.add(DecisionOutcome(label = "true", target = firstThen.target))
                    }
                    val firstElse = elseTargets.firstOrNull()
                    if (firstElse != null && firstElse.target != firstThen?.target) {
                        outcomes.add(DecisionOutcome(label = "false", target = firstElse.target))
                    }
                    if (outcomes.isNotEmpty()) {
                        result.add(
                            Decision(
                                condition = condition,
                                outcomes = outcomes,
                                line = SourceScanner.lineOf(content, match.range.first)
                            )
                        )
                    }
                }
            }
        return result
    }

    fun targetsIn(snippet: String): List<NavCall> =
        KotlinNavExtractor.navigationCalls(snippet) +
            KotlinNavExtractor.intentCalls(snippet) +
            KotlinNavExtractor.transactionCalls(snippet)
}