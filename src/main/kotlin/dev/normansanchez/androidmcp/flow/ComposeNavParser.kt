package dev.normansanchez.androidmcp.flow

data class ComposeRoute(
    val route: String,
    val line: Int,
    val screenCall: String?
)

data class ComposeNavCall(
    val target: String,
    val line: Int
)

object ComposeNavParser {

    private val composablePattern =
        Regex("""\bcomposable\s*\(\s*\n?\s*(?:route\s*=\s*)?["']([^"']+)["']""")
    private val startDestinationPattern =
        Regex("""startDestination\s*=\s*["']([^"']+)["']""")
    private val navigateRoutePattern =
        Regex("""\.navigate\s*\(\s*["']([^"']+)["']""")
    private val navigateResourcePattern =
        Regex("""\.navigate\s*\(\s*(R\.id\.([A-Za-z0-9_]+))""")

    fun routes(content: String): List<ComposeRoute> =
        composablePattern.findAll(content).map { match ->
            val openBrace = content.indexOf('{', match.range.last)
            val screenCall = if (openBrace == -1) {
                null
            } else {
                val closeBrace = KotlinNavExtractor.findClosingBrace(content, openBrace)
                val block = content.substring(openBrace, closeBrace)
                Regex("""(?:^|[\s{;])([A-Z][A-Za-z0-9_]*)\s*\(""")
                    .find(block)?.groupValues?.get(1)
            }
            ComposeRoute(
                route = match.groupValues[1],
                line = SourceScanner.lineOf(content, match.range.first),
                screenCall = screenCall
            )
        }.toList()

    fun startDestinations(content: String): List<String> =
        startDestinationPattern.findAll(content).map { it.groupValues[1] }.toList()

    fun navigateCalls(content: String): List<ComposeNavCall> {
        val calls = mutableListOf<ComposeNavCall>()
        navigateRoutePattern.findAll(content).forEach {
            calls.add(ComposeNavCall(target = it.groupValues[1], line = SourceScanner.lineOf(content, it.range.first)))
        }
        navigateResourcePattern.findAll(content).forEach {
            calls.add(ComposeNavCall(target = it.groupValues[1], line = SourceScanner.lineOf(content, it.range.first)))
        }
        return calls
    }
}