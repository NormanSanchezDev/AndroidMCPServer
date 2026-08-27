package dev.normansanchez.androidmcp.navigation

data class NavDestination(
    val id: String,
    val label: String?,
    val deepLinks: List<String>,
    val actions: List<NavAction>,
    val arguments: List<NavArgument>,
    val isStartDestination: Boolean
)

data class NavAction(
    val id: String,
    val destination: String
)

data class NavArgument(
    val name: String,
    val type: String?,
    val defaultValue: String?
)

data class NavGraph(
    val startDestination: String?,
    val destinations: List<NavDestination>
)

object NavXmlParser {

    fun parse(xmlContent: String): NavGraph {
        val destinations = mutableListOf<NavDestination>()

        val navGraphPattern = Regex(
            """<nav-graph\b[^>]*>(.*?)</nav-graph>""",
            RegexOption.DOT_MATCHES_ALL
        )

        val fragmentPattern = Regex(
            """<fragment\b[^>]*android:id="([^"]+)"[^>]*"""
        )

        val activityPattern = Regex(
            """<activity\b[^>]*android:id="([^"]+)"[^>]*"""
        )

        val startDestPattern = Regex("""app:startDestination="([^"]+)"""")
        val startDest = startDestPattern.find(xmlContent)?.groupValues?.get(1)

        val nodePattern = Regex(
            """<(fragment|activity)\b([^>]*?)/?>""",
            RegexOption.DOT_MATCHES_ALL
        )

        for (match in nodePattern.findAll(xmlContent)) {
            val attrs = match.groupValues[2]
            val id = Regex("""android:id="([^"]+)"""").find(attrs)?.groupValues?.get(1) ?: continue
            val label = Regex("""android:label="([^"]+)"""").find(attrs)?.groupValues?.get(1)

            val deepLinks = Regex("""<deep-link\b[^>]*android:uri="([^"]+)"[^>]*/>""").findAll(attrs)
                .map { it.groupValues[1] }.toList()

            val deepLinkBlock = Regex(
                """<deep-link\b[^>]*>(.*?)</deep-link>""",
                RegexOption.DOT_MATCHES_ALL
            ).findAll(attrs).map {
                Regex("""android:uri="([^"]+)"""").find(it.groupValues[1])?.groupValues?.get(1)
            }.filterNotNull().toList()

            val allDeepLinks = deepLinks + deepLinkBlock

            destinations.add(
                NavDestination(
                    id = id,
                    label = label,
                    deepLinks = allDeepLinks,
                    actions = emptyList(),
                    arguments = emptyList(),
                    isStartDestination = id == startDest
                )
            )
        }

        return NavGraph(
            startDestination = startDest,
            destinations = destinations
        )
    }
}
