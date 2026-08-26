package dev.normansanchez.androidmcp.tools

import java.io.StringReader
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.w3c.dom.Element
import org.xml.sax.InputSource

object EntryPointsFindTool {

    private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
    private val LAUNCHER_CATEGORIES =
        setOf("android.intent.category.LAUNCHER", "android.intent.category.LEANBACK_LAUNCHER")

    fun execute(
        projectRoot: String,
        module: String = "app"
    ): kotlinx.serialization.json.JsonObject {
        val root = Path.of(projectRoot).normalize().toAbsolutePath()

        if (!Files.isDirectory(root)) {
            return buildJsonObject {
                put("status", "invalid_project")
                put("projectRoot", root.toString())
            }
        }

        val manifestPath = root
            .resolve(module.removePrefix(":"))
            .resolve("src/main/AndroidManifest.xml")

        if (!Files.isRegularFile(manifestPath)) {
            return buildJsonObject {
                put("status", "not_available")
                put("projectRoot", root.toString())
                put("module", module)
                put(
                    "hint",
                    "No src/main/AndroidManifest.xml found for this module."
                )
            }
        }

        val document = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
        }
            .newDocumentBuilder()
            .parse(InputSource(StringReader(Files.readString(manifestPath))))

        val launchers = mutableListOf<kotlinx.serialization.json.JsonObject>()
        val deepLinks = mutableListOf<kotlinx.serialization.json.JsonObject>()
        var exportedCount = 0

        val activities = document.getElementsByTagName("activity")
        for (index in 0 until activities.length) {
            val activity = activities.item(index) as? Element ?: continue
            val name = activity.androidAttribute("name") ?: continue

            val filters = activity.getElementsByTagName("intent-filter")
            for (filterIndex in 0 until filters.length) {
                val filter = filters.item(filterIndex) as? Element ?: continue

                val actions = collectAndroidNames(filter, "action")
                val categories = collectAndroidNames(filter, "category")

                if (actions.contains("android.intent.action.MAIN") &&
                    categories.any { it in LAUNCHER_CATEGORIES }
                ) {
                    launchers.add(
                        buildJsonObject {
                            put("component", name)
                            put("kind", "activity")
                        }
                    )
                }

                if (actions.contains("android.intent.action.VIEW") &&
                    categories.contains("android.intent.category.BROWSABLE")
                ) {
                    deepLinks.add(buildDeepLink(name, filter))
                }
            }

            if (activity.exported() == true) {
                exportedCount += 1
            }
        }

        listOf("service", "receiver", "provider").forEach { tag ->
            val nodes = document.getElementsByTagName(tag)
            for (index in 0 until nodes.length) {
                val element = nodes.item(index) as? Element ?: continue
                if (element.exported() == true) {
                    exportedCount += 1
                }
            }
        }

        return buildJsonObject {
            put("status", "success")
            put("projectRoot", root.toString())
            put("module", module.removePrefix(":"))
            put(
                "manifest",
                root.relativize(manifestPath).toString()
            )

            put(
                "launchers",
                buildJsonArray { launchers.forEach { add(it) } }
            )
            put(
                "deepLinks",
                buildJsonArray { deepLinks.forEach { add(it) } }
            )
            put("exportedComponentCount", exportedCount)
        }
    }

    private fun buildDeepLink(
        component: String,
        filter: Element
    ): kotlinx.serialization.json.JsonObject {
        val schemes = mutableSetOf<String>()
        val hosts = mutableSetOf<String>()
        var autoVerify = false

        val dataNodes = filter.getElementsByTagName("data")
        for (index in 0 until dataNodes.length) {
            val data = dataNodes.item(index) as? Element ?: continue
            data.androidAttribute("scheme")?.let { schemes.add(it) }
            data.androidAttribute("host")?.let { hosts.add(it) }
        }

        autoVerify = filter.androidAttribute("autoVerify") == "true"

        return buildJsonObject {
            put("component", component)
            put(
                "schemes",
                buildJsonArray { schemes.sorted().forEach { add(JsonPrimitive(it)) } }
            )
            put(
                "hosts",
                buildJsonArray { hosts.sorted().forEach { add(JsonPrimitive(it)) } }
            )
            put("autoVerify", autoVerify)
        }
    }

    private fun collectAndroidNames(parent: Element, tagName: String): List<String> {
        val result = mutableListOf<String>()
        val nodes = parent.getElementsByTagName(tagName)
        for (index in 0 until nodes.length) {
            val element = nodes.item(index) as? Element ?: continue
            element.androidAttribute("name")?.let { result.add(it) }
        }
        return result
    }

    private fun Element.androidAttribute(name: String): String? =
        getAttributeNS(ANDROID_NS, name).takeIf { it.isNotBlank() }

    private fun Element.exported(): Boolean? =
        when (androidAttribute("exported")) {
            "true" -> true
            "false" -> false
            else -> null
        }
}
