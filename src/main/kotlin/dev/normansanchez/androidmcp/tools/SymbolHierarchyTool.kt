package dev.normansanchez.androidmcp.tools

import dev.normansanchez.androidmcp.symbol.HierarchyBuilder
import dev.normansanchez.androidmcp.symbol.HierarchyNode
import dev.normansanchez.androidmcp.symbol.KotlinSourceScanner
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.nio.file.Files
import java.nio.file.Path

object SymbolHierarchyTool {

    fun execute(projectRoot: String, className: String): kotlinx.serialization.json.JsonObject {
        val root = Path.of(projectRoot).normalize().toAbsolutePath()

        if (!Files.isDirectory(root)) {
            return buildJsonObject {
                put("status", "invalid_project")
                put("projectRoot", root.toString())
            }
        }

        val scannedFiles = KotlinSourceScanner.scan(root, includeTests = false)
        val entries = HierarchyBuilder.build(scannedFiles)

        if (!entries.containsKey(className)) {
            return buildJsonObject {
                put("status", "not_found")
                put("projectRoot", root.toString())
                put("className", className)
            }
        }

        val tree = HierarchyBuilder.buildHierarchyTree(entries, className)
            ?: return buildJsonObject {
                put("status", "error")
                put("projectRoot", root.toString())
                put("error", "Failed to build hierarchy for $className")
            }

        return buildJsonObject {
            put("status", "success")
            put("projectRoot", root.toString())
            put("className", className)
            put("totalClasses", entries.size)
            put("hierarchy", serializeNode(tree))
        }
    }

    private fun serializeNode(node: HierarchyNode): kotlinx.serialization.json.JsonObject {
        return buildJsonObject {
            put("name", node.name)
            put("kind", node.kind)
            put(
                "supertypes",
                buildJsonArray {
                    node.supertypes.forEach { add(JsonPrimitive(it)) }
                }
            )
            put("file", node.file)
            put("line", node.line)
            put(
                "children",
                buildJsonArray {
                    node.children.forEach { child -> add(serializeNode(child)) }
                }
            )
        }
    }
}
