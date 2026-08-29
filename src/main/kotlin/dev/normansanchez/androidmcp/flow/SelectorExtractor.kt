package dev.normansanchez.androidmcp.flow

data class LayoutAction(
    val resourceId: String,
    val text: String?,
    val contentDescription: String?,
    val onClickMethod: String?
)

data class ComposeAction(
    val label: String?,
    val selectors: List<Selector>,
    val line: Int,
    val body: String
)

object SelectorExtractor {

    private val clickableElements = setOf(
        "Button", "ImageButton", "FloatingActionButton", "TextButton",
        "MaterialButton", "MaterialToolbar", "Switch", "SwitchCompat",
        "CheckBox", "ToggleButton", "RadioButton"
    )

    fun layoutActions(layoutContent: String, strings: Map<String, String>): List<LayoutAction> {
        val actions = mutableListOf<LayoutAction>()
        Regex("""<([A-Za-z0-9_.]+)\b([^>]*?)/?>""", RegexOption.DOT_MATCHES_ALL)
            .findAll(layoutContent)
            .forEach { match ->
                val tag = match.groupValues[1].substringAfterLast('.')
                val attrs = match.groupValues[2]
                val id = Regex("""android:id\s*=\s*"@\+id/([A-Za-z0-9_]+)"""").find(attrs)
                if (id == null) return@forEach

                val hasOnClick = attrs.contains("android:onClick")
                val isClickableElement = tag in clickableElements || hasOnClick
                if (!isClickableElement) return@forEach

                val onClick = Regex("""android:onClick\s*=\s*"([^"]+)"""")
                    .find(attrs)?.groupValues?.get(1)
                val text = resolveString("android:text", attrs, strings)
                val contentDescription = resolveString("android:contentDescription", attrs, strings)

                actions.add(
                    LayoutAction(
                        resourceId = id.groupValues[1],
                        text = text,
                        contentDescription = contentDescription,
                        onClickMethod = onClick
                    )
                )
            }
        return actions
    }

    private fun resolveString(attr: String, attrs: String, strings: Map<String, String>): String? {
        val raw = Regex("""$attr\s*=\s*"([^"]+)"""").find(attrs)?.groupValues?.get(1)
            ?: return null
        if (raw.startsWith("@string/")) {
            return strings[raw.removePrefix("@string/")]
        }
        return raw.takeIf { it.isNotBlank() }
    }

    fun composeActions(content: String): List<ComposeAction> {
        val actions = mutableListOf<ComposeAction>()
        Regex("""(?:\.clickable\s*\{|onClick\s*=\s*\{)""")
            .findAll(content)
            .forEach { anchor ->
                val blockEnd = KotlinNavExtractor.findClosingBrace(content, anchor.range.last)
                val afterEnd = (blockEnd + 300).coerceAtMost(content.length)
                val selectors = mutableListOf<Selector>()
                val region = content.substring(blockEnd, afterEnd)
                Regex("""\.testTag\s*\(\s*"([^"]+)"\s*\)""").find(region)?.let {
                    selectors.add(Selector(SelectorKind.TEST_TAG, it.groupValues[1]))
                }
                Regex("""contentDescription\s*=\s*"([^"]+)"""").find(region)?.let {
                    if (it.groupValues[1].isNotBlank()) {
                        selectors.add(Selector(SelectorKind.CONTENT_DESCRIPTION, it.groupValues[1]))
                    }
                }
                val label = Regex("""Text\s*\(\s*["']([^"']{1,80})["']""")
                    .find(region)?.groupValues?.get(1)
                actions.add(
                    ComposeAction(
                        label = label,
                        selectors = selectors.distinct(),
                        line = SourceScanner.lineOf(content, anchor.range.first),
                        body = content.substring(anchor.range.last, blockEnd).take(300)
                    )
                )
            }
        return actions
    }
}