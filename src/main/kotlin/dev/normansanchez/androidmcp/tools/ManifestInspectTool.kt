package dev.normansanchez.androidmcp.tools

import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.w3c.dom.Element

object ManifestInspectTool {

    fun execute(projectRoot: String, modulePath: String = "app"): JsonObject {
        val root = Path.of(projectRoot)
            .normalize()
            .toAbsolutePath()

        val moduleRoot = root.resolve(modulePath).normalize()
        val manifestPath = moduleRoot.resolve("src/main/AndroidManifest.xml")

        if (!Files.isRegularFile(manifestPath)) {
            return buildJsonObject {
                put("status", "not_found")
                put("projectRoot", root.toString())
                put("module", modulePath)
                put("manifest", manifestPath.toString())
                put("error", "AndroidManifest.xml was not found")
            }
        }

        val documentBuilderFactory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
        }

        val document = Files.newInputStream(manifestPath).use { input ->
            documentBuilderFactory
                .newDocumentBuilder()
                .parse(input)
        }

        document.documentElement.normalize()

        val manifest = document.documentElement

        val packageName = manifest.getAttribute("package")
            .takeIf { it.isNotBlank() }

        val application = manifest
            .getElementsByTagName("application")
            .item(0) as? Element

        val applicationClass = application
            ?.androidAttribute("name")
            ?.takeIf { it.isNotBlank() }

        return buildJsonObject {
            put("status", "success")
            put("projectRoot", root.toString())
            put("module", modulePath)
            put(
                "manifest",
                root.relativize(manifestPath).toString()
            )

            packageName?.let {
                put("package", it)
            }

            applicationClass?.let {
                put("applicationClass", it)
            }

            put(
                "permissions",
                buildJsonArray {
                    val permissions =
                        manifest.getElementsByTagName("uses-permission")

                    for (index in 0 until permissions.length) {
                        val permission =
                            permissions.item(index) as? Element
                                ?: continue

                        permission.androidAttribute("name")
                            .takeIf { it.isNotBlank() }
                            ?.let { add(JsonPrimitive(it)) }
                    }
                }
            )

            put(
                "activities",
                buildJsonArray {
                    val activities =
                        manifest.getElementsByTagName("activity")

                    for (index in 0 until activities.length) {
                        val activity =
                            activities.item(index) as? Element
                                ?: continue

                        add(
                            buildJsonObject {
                                put(
                                    "name",
                                    activity.androidAttribute("name")
                                )

                                activity
                                    .androidAttribute("exported")
                                    .takeIf { it.isNotBlank() }
                                    ?.let {
                                        put(
                                            "exported",
                                            it.toBooleanStrictOrNull()
                                                ?: false
                                        )
                                    }

                                put(
                                    "intentFilters",
                                    activity.readIntentFilters()
                                )
                            }
                        )
                    }
                }
            )

            put(
                "services",
                readComponents(
                    application = application,
                    tagName = "service"
                )
            )

            put(
                "receivers",
                readComponents(
                    application = application,
                    tagName = "receiver"
                )
            )

            put(
                "providers",
                readComponents(
                    application = application,
                    tagName = "provider"
                )
            )
        }
    }

    private fun readComponents(
        application: Element?,
        tagName: String
    ) = buildJsonArray {
        if (application == null) {
            return@buildJsonArray
        }

        val nodes = application.getElementsByTagName(tagName)

        for (index in 0 until nodes.length) {
            val element = nodes.item(index) as? Element
                ?: continue

            add(
                buildJsonObject {
                    put(
                        "name",
                        element.androidAttribute("name")
                    )

                    element
                        .androidAttribute("exported")
                        .takeIf { it.isNotBlank() }
                        ?.let {
                            put(
                                "exported",
                                it.toBooleanStrictOrNull()
                                    ?: false
                            )
                        }
                }
            )
        }
    }

    private fun Element.readIntentFilters() =
        buildJsonArray {
            val filters = getElementsByTagName("intent-filter")

            for (filterIndex in 0 until filters.length) {
                val filter =
                    filters.item(filterIndex) as? Element
                        ?: continue

                add(
                    buildJsonObject {
                        put(
                            "actions",
                            buildJsonArray {
                                val actions =
                                    filter.getElementsByTagName("action")

                                for (
                                actionIndex in 0 until actions.length
                                ) {
                                    val action =
                                        actions.item(actionIndex)
                                                as? Element
                                            ?: continue

                                    action.androidAttribute("name")
                                        .takeIf { it.isNotBlank() }
                                        ?.let { add(JsonPrimitive(it)) }
                                }
                            }
                        )

                        put(
                            "categories",
                            buildJsonArray {
                                val categories =
                                    filter.getElementsByTagName(
                                        "category"
                                    )

                                for (
                                categoryIndex in
                                0 until categories.length
                                ) {
                                    val category =
                                        categories.item(categoryIndex)
                                                as? Element
                                            ?: continue

                                    category.androidAttribute("name")
                                        .takeIf { it.isNotBlank() }
                                        ?.let { add(JsonPrimitive(it)) }
                                }
                            }
                        )
                    }
                )
            }
        }

    private fun Element.androidAttribute(
        name: String
    ): String {
        return getAttributeNS(
            ANDROID_NAMESPACE,
            name
        )
    }

    private const val ANDROID_NAMESPACE =
        "http://schemas.android.com/apk/res/android"
}