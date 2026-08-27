package dev.normansanchez.androidmcp.tools

import dev.normansanchez.androidmcp.architecture.ArchitectureResult
import dev.normansanchez.androidmcp.architecture.PatternDetector
import dev.normansanchez.androidmcp.architecture.SourceFile
import dev.normansanchez.androidmcp.symbol.KotlinSourceScanner
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.nio.file.Files
import java.nio.file.Path

object ArchitectureDetectTool {

    fun execute(projectRoot: String): kotlinx.serialization.json.JsonObject {
        val root = Path.of(projectRoot).normalize().toAbsolutePath()

        if (!Files.isDirectory(root)) {
            return buildJsonObject {
                put("status", "invalid_project")
                put("projectRoot", root.toString())
            }
        }

        val scannedFiles = KotlinSourceScanner.scan(root, includeTests = false)
        if (scannedFiles.isEmpty()) {
            return buildJsonObject {
                put("status", "not_available")
                put("projectRoot", root.toString())
                put("error", "No Kotlin source files found")
            }
        }

        val sourceFiles = scannedFiles.map { SourceFile(it.relativePath, it.content) }
        val result = PatternDetector.detect(sourceFiles)

        return buildJsonObject {
            put("status", "success")
            put("projectRoot", root.toString())
            put("filesScanned", scannedFiles.size)
            put("diFramework", result.diFramework?.let { JsonPrimitive(it) } ?: JsonPrimitive(null as String?))
            put("usesCompose", result.usesCompose)
            put("viewModelPattern", result.viewModelPattern?.let { JsonPrimitive(it) } ?: JsonPrimitive(null as String?))
            put(
                "reactiveTypes",
                buildJsonArray {
                    result.reactiveTypes.forEach { add(JsonPrimitive(it)) }
                }
            )
            put("evidenceCount", result.diEvidence.size + result.composeEvidence.size +
                    result.viewModelEvidence.size + result.reactiveEvidence.size)
            put(
                "evidence",
                buildJsonArray {
                    (result.diEvidence + result.composeEvidence + result.viewModelEvidence + result.reactiveEvidence)
                        .take(50)
                        .forEach { e ->
                            add(buildJsonObject {
                                put("category", e.category)
                                put("name", e.name)
                                put("evidence", e.evidence)
                                put("file", e.file)
                                put("line", e.line)
                            })
                        }
                }
            )
        }
    }
}
