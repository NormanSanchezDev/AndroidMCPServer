package dev.normansanchez.androidmcp.dependencies

data class DependencyNode(
    val group: String?,
    val name: String,
    val version: String?,
    val requestedVersion: String?,
    val isProjectDependency: Boolean,
    val omittedByConflict: Boolean,
    val repeatedSubtreeOmitted: Boolean
)

data class ConfigurationDependencies(
    val configuration: String,
    val description: String?,
    val dependencies: List<DependencyNode>
)

object GradleDependenciesTreeParser {

    private val nodeLineRegex = Regex("^(.*?)[+\\\\]-{3}\\s+(.+)$")
    private val configurationHeaderRegex = Regex("^([a-zA-Z][a-zA-Z0-9_]*)\\s+-\\s+(.+)$")

    fun parse(output: String): List<ConfigurationDependencies> {
        val result = mutableListOf<ConfigurationDependencies>()
        var current: ConfigurationDependencies? = null
        val currentNodes = mutableListOf<DependencyNode>()

        fun flush() {
            current?.let { result.add(it.copy(dependencies = currentNodes.toList())) }
            currentNodes.clear()
        }

        for (rawLine in output.lineSequence()) {
            val line = rawLine.trimEnd()

            if (line.isBlank() || line.startsWith(">") || line.startsWith("---")) {
                continue
            }

            if (!line.startsWith("+") && !line.startsWith("\\") && !line.startsWith("|")) {
                // Possible new configuration header like "releaseRuntimeClasspath - ..."
                val headerMatch = configurationHeaderRegex.matchEntire(line)
                if (headerMatch != null && headerMatch.groupValues[1] != null) {
                    flush()
                    current = ConfigurationDependencies(
                        configuration = headerMatch.groupValues[1],
                        description = headerMatch.groupValues[2],
                        dependencies = emptyList()
                    )
                }
                continue
            }

            val nodeMatch = nodeLineRegex.matchEntire(line)
            if (nodeMatch != null && current != null) {
                parseNode(nodeMatch.groupValues[2])?.let { currentNodes.add(it) }
            }
        }

        flush()
        return result
    }

    private fun parseNode(raw: String): DependencyNode? {
        var text = raw.trim()

        val repeatedSubtree = text.contains("(*)")
        text = text.replace("(*)", "").trim()

        if (text.startsWith("(c)")) {
            return null
        }

        val nonResolved = text.endsWith("(n)")
        text = text.replace("(n)", "").trim()

        if (nonResolved || text.isBlank()) {
            return DependencyNode(
                group = null,
                name = text.ifBlank { "unresolved" },
                version = null,
                requestedVersion = null,
                isProjectDependency = false,
                omittedByConflict = false,
                repeatedSubtreeOmitted = true
            )
        }

        if (text.startsWith("project :") || text.startsWith("project ")) {
            return DependencyNode(
                group = null,
                name = text.removePrefix("project ").trim(),
                version = null,
                requestedVersion = null,
                isProjectDependency = true,
                omittedByConflict = false,
                repeatedSubtreeOmitted = repeatedSubtree
            )
        }

        val arrowIndex = text.indexOf("->")
        if (arrowIndex > 0) {
            val left = text.substring(0, arrowIndex).trim()
            val right = text.substring(arrowIndex + 2).trim()
            val leftParts = left.split(":")
            if (leftParts.size == 3) {
                val resolvedIsFullCoord = right.split(":").size == 3
                return DependencyNode(
                    group = leftParts[0],
                    name = leftParts[1],
                    version = if (resolvedIsFullCoord) right.split(":")[2] else right,
                    requestedVersion = leftParts[2],
                    isProjectDependency = false,
                    omittedByConflict = true,
                    repeatedSubtreeOmitted = repeatedSubtree
                )
            }
            return null
        }

        val parts = text.split(":")
        return when {
            parts.size == 3 ->
                simpleNode(parts[0], parts[1], parts[2], repeatedSubtree)

            parts.size == 2 ->
                simpleNode(null, parts[0], parts[1], repeatedSubtree)

            else -> null
        }
    }

    private fun simpleNode(
        group: String?,
        name: String,
        version: String?,
        repeated: Boolean
    ) = DependencyNode(
        group = group?.takeIf { it.isNotBlank() },
        name = name,
        version = version,
        requestedVersion = null,
        isProjectDependency = false,
        omittedByConflict = false,
        repeatedSubtreeOmitted = repeated
    )
}
