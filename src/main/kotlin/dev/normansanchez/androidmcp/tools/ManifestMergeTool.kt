package dev.normansanchez.androidmcp.tools

import dev.normansanchez.androidmcp.util.isUnderExcludedDir
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

object ManifestMergeTool {

    fun execute(projectRoot: String): kotlinx.serialization.json.JsonObject {
        val root = Path.of(projectRoot).normalize().toAbsolutePath()

        if (!Files.isDirectory(root)) {
            return buildJsonObject {
                put("status", "invalid_project")
                put("projectRoot", root.toString())
            }
        }

        val manifests = findManifests(root)
        if (manifests.isEmpty()) {
            return buildJsonObject {
                put("status", "not_available")
                put("projectRoot", root.toString())
                put("error", "No AndroidManifest.xml files found")
            }
        }

        val conflicts = detectConflicts(manifests, root)

        return buildJsonObject {
            put("status", "success")
            put("projectRoot", root.toString())
            put("manifestCount", manifests.size)
            put(
                "manifests",
                buildJsonArray {
                    manifests.forEach { (module, path) ->
                        add(buildJsonObject {
                            put("module", module)
                            put("file", root.relativize(path).toString())
                        })
                    }
                }
            )
            put("conflictCount", conflicts.size)
            put(
                "conflicts",
                buildJsonArray {
                    conflicts.forEach { conflict ->
                        add(buildJsonObject {
                            put("type", conflict.type)
                            put("component", conflict.component)
                            put("attribute", conflict.attribute)
                            put(
                                "values",
                                buildJsonArray {
                                    conflict.values.forEach { (module, value) ->
                                        add(buildJsonObject {
                                            put("module", module)
                                            put("value", value)
                                        })
                                    }
                                }
                            )
                        })
                    }
                }
            )
        }
    }

    private fun findManifests(root: Path): Map<String, Path> {
        val manifests = mutableMapOf<String, Path>()

        Files.walk(root, 6).use { paths ->
            paths.filter { Files.isRegularFile(it) && it.fileName.toString() == "AndroidManifest.xml" }
                .filter { !it.isUnderExcludedDir(root) }
                .filter { it.toString().contains("src/main") }
                .forEach { path ->
                    val module = extractModuleName(root, path)
                    manifests[module] = path
                }
        }

        return manifests
    }

    private fun extractModuleName(root: Path, manifestPath: Path): String {
        val relative = root.relativize(manifestPath).toString()
        val parts = relative.replace("\\", "/").split("/")
        return parts.firstOrNull() ?: "."
    }

    private fun detectConflicts(
        manifests: Map<String, Path>,
        root: Path
    ): List<MergeConflict> {
        val conflicts = mutableListOf<MergeConflict>()

        val componentMap = mutableMapOf<String, MutableMap<String, MutableMap<String, AttrValue>>>()

        for ((module, path) in manifests) {
            val parsed = parseManifest(path) ?: continue

            for ((componentType, components) in parsed) {
                for ((componentName, attributes) in components) {
                    val attrs = componentMap.getOrPut(componentType) { mutableMapOf() }
                        .getOrPut(componentName) { mutableMapOf() }

                    for ((attrName, attrValue) in attributes) {
                        val existing = attrs[attrName]
                        if (existing != null && existing.value != attrValue) {
                            conflicts.add(
                                MergeConflict(
                                    type = componentType,
                                    component = componentName,
                                    attribute = attrName,
                                    values = mapOf(
                                        existing.origin to existing.value,
                                        module to attrValue
                                    )
                                )
                            )
                        }
                        attrs[attrName] = AttrValue(attrValue, module)
                    }
                }
            }
        }

        return conflicts
    }

    private data class AttrValue(
        val value: String,
        val origin: String
    )

    private fun parseManifest(path: Path): Map<String, Map<String, Map<String, String>>>? {
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
        }

        val document = try {
            Files.newInputStream(path).use { input ->
                factory.newDocumentBuilder().parse(input)
            }
        } catch (_: Exception) {
            return null
        }

        val result = mutableMapOf<String, MutableMap<String, MutableMap<String, String>>>()

        val androidNs = "http://schemas.android.com/apk/res/android"

        val componentTags = listOf("activity", "service", "receiver", "provider")

        for (tag in componentTags) {
            val nodes = document.getElementsByTagName(tag)
            for (index in 0 until nodes.length) {
                val element = nodes.item(index) as? Element ?: continue
                val name = element.getAttributeNS(androidNs, "name").takeIf { it.isNotBlank() } ?: continue

                val attrs = mutableMapOf<String, String>()
                attrs["name"] = name

                for (attr in listOf("exported", "permission", "enabled", "directBootAware")) {
                    val value = element.getAttributeNS(androidNs, attr).takeIf { it.isNotBlank() }
                    if (value != null) {
                        attrs[attr] = value
                    }
                }

                result.getOrPut(tag) { mutableMapOf() }[name] = attrs
            }
        }

        return result
    }

    private data class MergeConflict(
        val type: String,
        val component: String,
        val attribute: String,
        val values: Map<String, String>
    )
}
