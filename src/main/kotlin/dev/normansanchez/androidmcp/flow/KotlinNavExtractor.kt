package dev.normansanchez.androidmcp.flow

data class NavCall(
    val target: String,
    val line: Int,
    val kind: String
)

data class ClickHandler(
    val resourceId: String,
    val line: Int,
    val body: String
)

object KotlinNavExtractor {

    private val intentPattern =
        Regex("""Intent\s*\(\s*[^,\n]*,\s*([A-Za-z0-9_$.]+)::class\.java\s*\)""")
    private val transactionPattern =
        Regex("""\b(replace|add|show)\s*\(\s*[^()]*?([A-Za-z0-9_$.]+)\(\)""")
    private val navigatePattern =
        Regex("""\bnavigate\s*\(\s*((?:R\.id\.([A-Za-z0-9_]+)|R\.navigation\.[A-Za-z0-9_]+|["']([^"']+)["']))""")
    private val clickListenerPattern =
        Regex("""\.setOnClickListener\s*\{""")

    fun intentCalls(content: String): List<NavCall> =
        intentPattern.findAll(content).mapNotNull { match ->
            val target = SourceScanner.simpleName(match.groupValues[1])
            if (target.isBlank()) null else NavCall(target, SourceScanner.lineOf(content, match.range.first), "intent")
        }.toList()

    fun transactionCalls(content: String): List<NavCall> =
        transactionPattern.findAll(content).mapNotNull { match ->
            val target = SourceScanner.simpleName(match.groupValues[2])
            if (target.isBlank()) null else NavCall(target, SourceScanner.lineOf(content, match.range.first), "fragment_transaction")
        }.toList()

    fun navigationCalls(content: String): List<NavCall> =
        navigatePattern.findAll(content).mapNotNull { match ->
            val target = when {
                match.groupValues[2].isNotBlank() -> match.groupValues[1]
                else -> match.groupValues[3].takeIf { it.isNotBlank() }
            } ?: return@mapNotNull null
            NavCall(target, SourceScanner.lineOf(content, match.range.first), "navigation")
        }.toList()

    fun clickHandlers(content: String): List<ClickHandler> {
        val handlers = mutableListOf<ClickHandler>()
        val ids = Regex("""R\.id\.([A-Za-z0-9_]+)""").findAll(content).map {
            it.groupValues[1] to it.range.last
        }.toList()
        for (listener in clickListenerPattern.findAll(content)) {
            val precedingId = ids.filter { it.second < listener.range.first }
                .maxByOrNull { it.second }
            if (precedingId == null) continue
            val bodyStart = listener.range.last
            val bodyEnd = findClosingBrace(content, bodyStart)
            handlers.add(
                ClickHandler(
                    resourceId = precedingId.first,
                    line = SourceScanner.lineOf(content, listener.range.first),
                    body = content.substring(bodyStart, bodyEnd).take(400)
                )
            )
        }
        return handlers
    }

    fun findClosingBrace(content: String, openOffset: Int): Int {
        var index = openOffset
        var depth = 0
        while (index < content.length) {
            when (content[index]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return index
                }
            }
            index++
        }
        return content.length
    }
}