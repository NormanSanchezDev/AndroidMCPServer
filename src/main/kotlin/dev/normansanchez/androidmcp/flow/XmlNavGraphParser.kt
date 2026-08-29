package dev.normansanchez.androidmcp.flow

import java.io.StringReader
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.InputSource

data class NavXmlDestination(
    val resourceId: String,
    val className: String?,
    val label: String?,
    val type: String,
    val deepLinks: List<String>,
    val isStartDestination: Boolean
)

data class NavXmlAction(
    val from: String,
    val to: String,
    val actionId: String
)

data class NavXmlGraph(
    val file: String,
    val graphId: String?,
    val startDestination: String?,
    val destinations: List<NavXmlDestination>,
    val actions: List<NavXmlAction>
)

object XmlNavGraphParser {

    private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
    private const val APP_NS = "http://schemas.android.com/apk/res-auto"

    fun parseFile(navigationFile: Path, root: Path): NavXmlGraph? {
        val content = try {
            Files.readString(navigationFile)
        } catch (e: Exception) {
            return null
        }
        if (!content.contains("startDestination") && !content.contains("<navigation") && !content.contains("<nav-graph")) {
            return null
        }
        return parseContent(content, root.relativize(navigationFile).toString())
    }

    fun parseContent(content: String, fileName: String): NavXmlGraph? {
        val document = try {
            val factory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
            val builder = factory.newDocumentBuilder()
            builder.parse(InputSource(StringReader(content)))
        } catch (e: Exception) {
            return null
        }
        document.documentElement.normalize()

        val graphElements = document.getElementsByTagName("navigation")
        if (graphElements.length == 0) {
            val legacy = document.getElementsByTagName("nav-graph")
            if (legacy.length == 0) return null
            return parseGraph(legacy.item(0) as? Element ?: return null, fileName)
        }
        return parseGraph(graphElements.item(0) as? Element ?: return null, fileName)
    }

    private fun parseGraph(top: Element, fileName: String): NavXmlGraph {
        val startDestination = top.appAttribute("startDestination").stripResourceId()
        val destinations = mutableListOf<NavXmlDestination>()
        val actions = mutableListOf<NavXmlAction>()

        fun walk(root: Element) {
            root.childNodes.forEachAction { child ->
                when (child.tagName) {
                    "fragment", "activity" -> {
                        val resourceId = child.androidAttribute("id").stripResourceId() ?: return@forEachAction
                        val destination = NavXmlDestination(
                            resourceId = resourceId,
                            className = child.androidAttribute("name").takeIf { it.isNotBlank() },
                            label = child.androidAttribute("label").takeIf { it.isNotBlank() },
                            type = child.tagName,
                            deepLinks = child.readDeepLinks(),
                            isStartDestination = resourceId == startDestination
                        )
                        destinations.add(destination)
                        child.childNodes.forEachAction { actionElement ->
                            if (actionElement.tagName == "action") {
                                actionElement.appAttribute("destination")
                                    .stripResourceId()
                                    ?.let { target ->
                                        actions.add(
                                            NavXmlAction(
                                                from = resourceId,
                                                to = target,
                                                actionId = actionElement.androidAttribute("id")
                                                    .stripResourceId().orEmpty()
                                            )
                                        )
                                    }
                            }
                        }
                    }

                    "navigation" -> walk(child)
                }
            }
        }

        walk(top)
        return NavXmlGraph(
            file = fileName,
            graphId = top.androidAttribute("id").stripResourceId(),
            startDestination = startDestination,
            destinations = destinations,
            actions = actions
        )
    }

    private fun Element.readDeepLinks(): List<String> {
        val links = mutableListOf<String>()
        childNodes.forEachAction { element ->
            if (element.tagName == "deepLink") {
                element.appAttribute("uri").takeIf { it.isNotBlank() }?.let { links.add(it) }
            }
        }
        return links
    }

    private fun NodeList.forEachAction(action: (Element) -> Unit) {
        for (index in 0 until length) {
            (item(index) as? Element)?.let { action(it) }
        }
    }

    private fun String?.stripResourceId(): String? {
        if (this == null) return null
        val trimmed = trim()
        if (trimmed.isEmpty()) return null
        return trimmed.substringAfterLast("/")
    }

    private fun Element.androidAttribute(name: String): String =
        getAttributeNS(ANDROID_NS, name)

    private fun Element.appAttribute(name: String): String =
        getAttributeNS(APP_NS, name)
}