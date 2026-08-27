package dev.normansanchez.androidmcp.tools

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name

object ResourceReferencesTool {

    fun execute(projectRoot: String, resourceName: String): kotlinx.serialization.json.JsonObject {
        val root = Path.of(projectRoot).normalize().toAbsolutePath()

        if (!Files.isDirectory(root)) {
            return buildJsonObject {
                put("status", "invalid_project")
                put("projectRoot", root.toString())
            }
        }

        val parts = resourceName.split(".", limit = 2)
        val resType = parts.getOrNull(0) ?: ""
        val resName = parts.getOrNull(1) ?: resourceName

        val kotlinRefs = findKotlinReferences(root, resType, resName)
        val xmlRefs = findXmlReferences(root, resType, resName)

        return buildJsonObject {
            put("status", "success")
            put("projectRoot", root.toString())
            put("resourceName", resourceName)
            put("totalReferences", kotlinRefs.size + xmlRefs.size)
            put(
                "kotlinReferences",
                buildJsonArray {
                    kotlinRefs.forEach { ref ->
                        add(buildJsonObject {
                            put("file", ref.file)
                            put("line", ref.line)
                            put("context", ref.context)
                        })
                    }
                }
            )
            put(
                "xmlReferences",
                buildJsonArray {
                    xmlRefs.forEach { ref ->
                        add(buildJsonObject {
                            put("file", ref.file)
                            put("line", ref.line)
                            put("context", ref.context)
                        })
                    }
                }
            )
        }
    }

    private fun findKotlinReferences(root: Path, resType: String, resName: String): List<Ref> {
        val refs = mutableListOf<Ref>()

        Files.walk(root, 8).use { paths ->
            paths.filter { Files.isRegularFile(it) }
                .filter { it.name.endsWith(".kt") || it.name.endsWith(".java") }
                .forEach { file ->
                    val content = try { Files.readString(file) } catch (_: Exception) { return@forEach }
                    val pattern = Regex("""R\.$resType\.$resName""")
                    val lines = content.lines()
                    for ((index, line) in lines.withIndex()) {
                        if (pattern.containsMatchIn(line)) {
                            refs.add(
                                Ref(
                                    file = root.relativize(file).toString(),
                                    line = index + 1,
                                    context = line.trim()
                                )
                            )
                        }
                    }
                }
        }

        return refs
    }

    private fun findXmlReferences(root: Path, resType: String, resName: String): List<Ref> {
        val refs = mutableListOf<Ref>()

        Files.walk(root, 8).use { paths ->
            paths.filter { Files.isRegularFile(it) }
                .filter { it.name.endsWith(".xml") }
                .forEach { file ->
                    val content = try { Files.readString(file) } catch (_: Exception) { return@forEach }
                    val pattern = Regex("""@$resType/$resName""")
                    val lines = content.lines()
                    for ((index, line) in lines.withIndex()) {
                        if (pattern.containsMatchIn(line)) {
                            refs.add(
                                Ref(
                                    file = root.relativize(file).toString(),
                                    line = index + 1,
                                    context = line.trim()
                                )
                            )
                        }
                    }
                }
        }

        return refs
    }

    private data class Ref(
        val file: String,
        val line: Int,
        val context: String
    )
}
