package dev.normansanchez.androidmcp.navigation

import dev.normansanchez.androidmcp.symbol.KotlinSourceScanner
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

    private val COMPOSABLE_PATTERN = Regex("""composable\(\s*"([^"]+)"""")

    fun detect(sourceFiles: List<ScannedKotlinFile>): List<ComposeRoute> {
        val routes = mutableListOf<ComposeRoute>()

        for (file in sourceFiles) {
            val lines = file.content.lines()
            for ((index, line) in lines.withIndex()) {
                COMPOSABLE_PATTERN.findAll(line).forEach { match ->
                    routes.add(
                        ComposeRoute(
                            route = match.groupValues[1],
                            file = file.relativePath,
                            line = index + 1
                        )
                    )
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
