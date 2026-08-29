package dev.normansanchez.androidmcp.navigation

import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.xml.sax.InputSource

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

    private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
    private const val APP_NS = "http://schemas.android.com/apk/res-auto"

    fun parse(xmlContent: String): NavGraph {
        val document = try {
            val factory = DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = true
                isExpandEntityReferences = false
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            }
            factory.newDocumentBuilder().parse(InputSource(StringReader(xmlContent)))
        } catch (_: Exception) {
            return NavGraph(startDestination = null, destinations = emptyList())
        }

        val root = document.documentElement
        val startDest = normalizeId(root.appAttr("startDestination"))

        val destinations = mutableListOf<NavDestination>()

        for (tag in listOf("fragment", "activity")) {
            for (index in 0 until root.getElementsByTagName(tag).length) {
                val element = root.getElementsByTagName(tag).item(index) as? Element ?: continue
                val id = element.androidAttr("id")?.let { normalizeId(it) } ?: continue

                val deepLinks = element.getElementsByTagName("deep-link").let { nodes ->
                    buildList {
                        for (childIndex in 0 until nodes.length) {
                            val node = nodes.item(childIndex) as? Element ?: continue
                            val uri = node.appAttr("uri") ?: node.androidAttr("uri")
                            if (!uri.isNullOrBlank()) add(uri)
                        }
                    }
                }

                val actions = element.getElementsByTagName("action").let { nodes ->
                    buildList {
                        for (childIndex in 0 until nodes.length) {
                            val node = nodes.item(childIndex) as? Element ?: continue
                            val actionId = node.androidAttr("id")?.let { normalizeId(it) }
                            val destination = node.appAttr("destination")?.let { normalizeId(it) }
                                ?: node.androidAttr("destination")?.let { normalizeId(it) }
                            if (actionId != null && destination != null) {
                                add(NavAction(id = actionId, destination = destination))
                            }
                        }
                    }
                }

                val arguments = element.getElementsByTagName("argument").let { nodes ->
                    buildList {
                        for (childIndex in 0 until nodes.length) {
                            val node = nodes.item(childIndex) as? Element ?: continue
                            val name = node.androidAttr("name") ?: continue
                            add(
                                NavArgument(
                                    name = name,
                                    type = node.appAttr("argType")?.takeIf { it.isNotBlank() },
                                    defaultValue = node.androidAttr("defaultValue")?.takeIf { it.isNotBlank() }
                                )
                            )
                        }
                    }
                }

                destinations.add(
                    NavDestination(
                        id = id,
                        label = element.androidAttr("label"),
                        deepLinks = deepLinks,
                        actions = actions,
                        arguments = arguments,
                        isStartDestination = startDest != null && id == startDest
                    )
                )
            }
        }

        return NavGraph(
            startDestination = startDest,
            destinations = destinations
        )
    }

    private fun Element.androidAttr(name: String): String? =
        getAttributeNS(ANDROID_NS, name).takeIf { it.isNotBlank() }

    private fun Element.appAttr(name: String): String? =
        getAttributeNS(APP_NS, name).takeIf { it.isNotBlank() }

    private fun normalizeId(value: String?): String? {
        if (value == null) return null
        val trimmed = value.trim()
        return when {
            trimmed.startsWith("@+id/") -> trimmed.removePrefix("@+id/")
            trimmed.startsWith("@id/") -> trimmed.removePrefix("@id/")
            else -> trimmed
        }
    }
}