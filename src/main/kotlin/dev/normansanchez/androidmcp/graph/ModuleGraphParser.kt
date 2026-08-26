package dev.normansanchez.androidmcp.graph

data class ModuleEdge(
    val from: String,
    val to: String,
    val configuration: String?
)

data class ModuleGraph(
    val rootProjectName: String?,
    val modules: List<String>,
    val edges: List<ModuleEdge>
)

object ModuleGraphParser {

    private val quotedStringRegex = Regex("[\"']([^\"']+)[\"']")
    private val includeBlockRegex = Regex("""include\s*\(([^)]*)\)""")
    private val groovyIncludeRegex = Regex("""include\s+['"]([^'"]+)['"]""")
    private val rootNameRegex = Regex("""rootProject\.name\s*=\s*["']([^"']+)["']""")
    private val projectRefRegex = Regex("""project\s*\(\s*["']([^"']+)["']\s*\)""")
    private val configurationNames = setOf(
        "api", "implementation", "compileOnly", "runtimeOnly",
        "testImplementation", "androidTestImplementation", "debugImplementation",
        "releaseImplementation", "kapt", "ksp", "lintChecks", "coreLibraryDesugaring"
    )

    fun parseSettings(settingsContent: String): Pair<String?, List<String>> {
        val rootName = rootNameRegex.find(settingsContent)?.groupValues?.get(1)

        val modules = linkedSetOf<String>()

        includeBlockRegex.findAll(settingsContent).forEach { match ->
            quotedStringRegex.findAll(match.groupValues[1]).forEach {
                addModule(modules, it.groupValues[1])
            }
        }

        groovyIncludeRegex.findAll(settingsContent).forEach { match ->
            addModule(modules, match.groupValues[1])
        }

        return rootName to modules.toList()
    }

    private fun addModule(modules: LinkedHashSet<String>, rawPath: String) {
        val normalized = rawPath.trim().removePrefix(":")
        if (normalized.isNotBlank()) {
            modules.add(normalized)
        }
    }

    fun parseBuildFile(moduleName: String, buildFileContent: String): List<ModuleEdge> {
        val edges = mutableListOf<ModuleEdge>()

        projectRefRegex.findAll(buildFileContent).forEach { match ->
            val target = match.groupValues[1].removePrefix(":")
            val prefix = buildFileContent.substring(0, match.range.first)
            val lineStart = prefix.lastIndexOf('\n') + 1
            val currentLine = buildFileContent.substring(lineStart, match.range.first)

            val configuration = configurationNames.firstOrNull { configName ->
                Regex("""\b$configName\b\s*\(\s*$""").containsMatchIn(currentLine.trim())
            }

            edges.add(
                ModuleEdge(
                    from = moduleName,
                    to = target,
                    configuration = configuration
                )
            )
        }

        return edges
    }
}
