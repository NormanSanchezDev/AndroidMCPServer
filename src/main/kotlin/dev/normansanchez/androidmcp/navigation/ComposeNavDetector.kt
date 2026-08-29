package dev.normansanchez.androidmcp.navigation

import dev.normansanchez.androidmcp.symbol.ScannedKotlinFile

data class ComposeRoute(
    val route: String,
    val file: String,
    val line: Int
)

object ComposeNavDetector {

    private val NAV_HOST_PATTERNS = listOf(
        "NavHost",
        "NavHost(",
        "rememberNavController()"
    )

    private val COMPOSABLE_PATTERN = Regex(
        """\bcomposable\s*\(\s*(?:route\s*=\s*)?["']([^"']+)["']""",
        RegexOption.DOT_MATCHES_ALL
    )

    private val NAVIGATE_PATTERN = Regex(
        """\.navigate\s*\(\s*(?:route\s*=\s*)?["']([^"']+)["']""",
        RegexOption.DOT_MATCHES_ALL
    )

    fun detect(sourceFiles: List<ScannedKotlinFile>): List<ComposeRoute> {
        val routes = mutableListOf<ComposeRoute>()
        val seen = mutableSetOf<Triple<String, String, Int>>()

        for (file in sourceFiles) {
            for (pattern in listOf(COMPOSABLE_PATTERN, NAVIGATE_PATTERN)) {
                for (match in pattern.findAll(file.content)) {
                    val route = match.groupValues[1]
                    val line = file.content
                        .substring(0, match.range.first.coerceAtMost(file.content.length))
                        .count { it == '\n' } + 1
                    if (seen.add(Triple(route, file.relativePath, line))) {
                        routes.add(
                            ComposeRoute(
                                route = route,
                                file = file.relativePath,
                                line = line
                            )
                        )
                    }
                }
            }
        }

        return routes
    }

    fun hasNavHost(sourceFiles: List<ScannedKotlinFile>): Boolean {
        return sourceFiles.any { file ->
            NAV_HOST_PATTERNS.any { pattern -> file.content.contains(pattern) }
        }
    }
}