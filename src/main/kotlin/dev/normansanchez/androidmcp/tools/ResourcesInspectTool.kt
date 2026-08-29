package dev.normansanchez.androidmcp.tools

import dev.normansanchez.androidmcp.util.resolveModuleOrNull
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.w3c.dom.Element
import org.xml.sax.InputSource
import kotlin.io.path.extension
import kotlin.io.path.name

object ResourcesInspectTool {

    private val KNOWN_TYPES = setOf(
        "values", "drawable", "mipmap", "layout", "raw", "xml",
        "font", "anim", "animator", "color", "menu", "navigation", "transition"
    )

    private val DPI_BUCKETS = listOf(
        "ldpi", "mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi",
        "tvdpi", "nodpi", "anydpi", "anydpi-v26"
    )

    private const val MAX_NAMES_PER_TYPE = 300

    fun execute(
        projectRoot: String,
        module: String = "app",
        includeNames: Boolean = true
    ): kotlinx.serialization.json.JsonObject {
        val root = Path.of(projectRoot).normalize().toAbsolutePath()

        if (!Files.isDirectory(root)) {
            return buildJsonObject {
                put("status", "invalid_project")
                put("projectRoot", root.toString())
            }
        }

        val moduleDir = root.resolveModuleOrNull(module)
            ?: return buildJsonObject {
                put("status", "invalid_module")
                put("projectRoot", root.toString())
                put("module", module)
            }
        val resDir = moduleDir.resolve("src/main/res")

        if (!Files.isDirectory(resDir)) {
            return buildJsonObject {
                put("status", "not_available")
                put("projectRoot", root.toString())
                put("module", module)
                put("resDirectory", resDir.toString())
                put(
                    "hint",
                    "No src/main/res directory found for this module."
                )
            }
        }

        val folders = mutableListOf<kotlinx.serialization.json.JsonObject>()
        val namesByTag = linkedMapOf<String, MutableSet<String>>()
        var totalFiles = 0

        Files.list(resDir).use { entries ->
            entries.filter(Files::isDirectory).sorted().forEach { folder ->
                val folderName = folder.fileName.toString()

                var type = folderName.substringBefore("-")
                if (type !in KNOWN_TYPES) {
                    type = folderName
                }
                val qualifier = folderName.substringAfter("-", "").takeIf { it.isNotBlank() }

                val fileCount = Files.walk(folder, 1).use { stream ->
                    stream.filter { Files.isRegularFile(it) }.count().toInt()
                }
                totalFiles += fileCount

                folders.add(
                    buildJsonObject {
                        put("name", folderName)
                        put("type", type)
                        qualifier?.let { put("qualifier", it) }
                        if (type in DPI_BUCKETS || qualifier in DPI_BUCKETS) {
                            put("dpiBucket", qualifier ?: "unspecified")
                        }
                        put("fileCount", fileCount)
                    }
                )

                if (folderName.startsWith("values") && includeNames) {
                    collectValueNames(folder, namesByTag)
                }
            }
        }

        return buildJsonObject {
            put("status", "success")
            put("projectRoot", root.toString())
            put("module", module.removePrefix(":"))
            put(
                "resDirectory",
                try {
                    root.relativize(resDir).toString()
                } catch (_: Exception) {
                    resDir.toString()
                }
            )
            put("folderCount", folders.size)
            put("fileCount", totalFiles)

            put(
                "folders",
                buildJsonArray { folders.forEach { add(it) } }
            )

            if (includeNames && namesByTag.isNotEmpty()) {
                put(
                    "valueItems",
                    buildJsonArray {
                        namesByTag.forEach { (tag, names) ->
                            add(
                                buildJsonObject {
                                    put("tag", tag)
                                    put("count", names.size)
                                    put(
                                        "names",
                                        buildJsonArray {
                                            names.sorted()
                                                .take(MAX_NAMES_PER_TYPE)
                                                .forEach { add(JsonPrimitive(it)) }
                                        }
                                    )
                                    put(
                                        "namesTruncated",
                                        names.size > MAX_NAMES_PER_TYPE
                                    )
                                }
                            )
                        }
                    }
                )
            }
        }
    }

    private fun collectValueNames(
        valuesFolder: Path,
        namesByTag: MutableMap<String, MutableSet<String>>
    ) {
        Files.walk(valuesFolder, 1).use { files ->
            files.filter { Files.isRegularFile(it) && it.extension == "xml" }.forEach { xml ->
                try {
                    val document = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder()
                        .parse(InputSource(java.io.StringReader(Files.readString(xml))))

                    val children = document.documentElement.childNodes
                    for (index in 0 until children.length) {
                        val node = children.item(index) as? Element ?: continue
                        val name = node.getAttribute("name").takeIf { it.isNotBlank() } ?: continue
                        namesByTag
                            .getOrPut(node.tagName) { mutableSetOf() }
                            .add(name)
                    }
                } catch (_: Exception) {
                }
            }
        }
    }
}
