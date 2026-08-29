package dev.normansanchez.androidmcp.flow

object UiFrameworkDetector {

    fun detect(content: String): UiFramework {
        val compose = content.contains("@Composable")
            || content.contains("setContent")
            || content.contains("NavHost")
            || content.contains(".testTag(")
        val xml = content.contains("setContentView")
            || content.contains("LayoutInflater")
            || content.contains("inflate(R.layout")
        return when {
            compose && xml -> UiFramework.MIXED
            compose -> UiFramework.COMPOSE
            xml -> UiFramework.XML
            else -> UiFramework.UNKNOWN
        }
    }

    fun layoutNames(content: String): List<String> =
        Regex("""(?:setContentView|inflate)\s*\(\s*R\.layout\.([A-Za-z0-9_]+)""")
            .findAll(content)
            .map { it.groupValues[1] }
            .toSet()
            .toList()

    fun navHostFragmentGraph(layoutContent: String): String? {
        if (!layoutContent.contains("NavHostFragment")) return null
        return Regex("""app:navGraph\s*=\s*"@navigation/([A-Za-z0-9_]+)"""")
            .find(layoutContent)
            ?.groupValues
            ?.get(1)
    }
}