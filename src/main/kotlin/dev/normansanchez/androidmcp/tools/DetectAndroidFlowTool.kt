package dev.normansanchez.androidmcp.tools

import dev.normansanchez.androidmcp.flow.AndroidFlowIr
import dev.normansanchez.androidmcp.flow.DetectFlowOptions
import dev.normansanchez.androidmcp.flow.FlowDetector
import dev.normansanchez.androidmcp.flow.MermaidRenderer
import java.nio.file.Path
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

object DetectAndroidFlowTool {

    private val json = Json { prettyPrint = false }

    fun execute(
        projectRoot: String,
        scope: String?,
        entryPoint: String?,
        maxDepth: Int?,
        context: JsonObject?,
        includeMermaid: Boolean
    ): JsonObject {
        val root = Path.of(projectRoot).normalize().toAbsolutePath()
        val options = DetectFlowOptions(
            scope = scope ?: "application",
            entryPoint = entryPoint ?: "auto",
            maxDepth = maxDepth ?: 10,
            knownFeatures = context?.get("known_features")?.let { parseStringList(it) } ?: emptyList(),
            knownEntryPoints = context?.get("known_entry_points")?.let { parseStringList(it) } ?: emptyList()
        )

        val ir = FlowDetector.detect(root, options)

        return buildJsonObject {
            put("status", "success")
            put("projectRoot", root.toString())
            put("flowIR", encodeIr(ir))
            if (includeMermaid) {
                put("mermaid", MermaidRenderer.renderAll(ir.flows))
            }
        }
    }

    private fun encodeIr(ir: AndroidFlowIr): JsonObject {
        val encoded = try {
            json.encodeToString(AndroidFlowIr.serializer(), ir)
        } catch (e: SerializationException) {
            return buildJsonObject { put("error", e.message ?: "serialization failed") }
        }
        return (json.parseToJsonElement(encoded) as? JsonObject)
            ?: buildJsonObject { put("error", "unexpected serialization output") }
    }

    private fun parseStringList(element: kotlinx.serialization.json.JsonElement): List<String> =
        try {
            element.jsonArray.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
        } catch (_: Exception) {
            emptyList()
        }
}