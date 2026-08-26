package dev.normansanchez.androidmcp.tools

import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import dev.normansanchez.androidmcp.symbol.KotlinSourceScanner
import dev.normansanchez.androidmcp.symbol.SymbolExtractor

object SymbolFindTool {

    private val VALID_KINDS = setOf("class", "interface", "enum", "object", "function", "property", "typealias")

    fun execute(
        projectRoot: String,
        query: String,
        kind: String? = null,
        exactMatch: Boolean = false,
        includeTests: Boolean = false,
        maxResults: Int = 50
    ): kotlinx.serialization.json.JsonObject {
        val root = Path.of(projectRoot).normalize().toAbsolutePath()

        if (!Files.isDirectory(root)) {
            return buildJsonObject {
                put("status", "invalid_project")
                put("projectRoot", root.toString())
            }
        }

        if (query.isBlank()) {
            return buildJsonObject {
                put("status", "invalid_request")
                put("error", "query is required")
            }
        }

        if (kind != null && kind !in VALID_KINDS) {
            return buildJsonObject {
                put("status", "invalid_kind")
                put("kind", kind)
                put(
                    "validKinds",
                    kotlinx.serialization.json.buildJsonArray {
                        VALID_KINDS.sorted().forEach {
                            add(kotlinx.serialization.json.JsonPrimitive(it))
                        }
                    }
                )
            }
        }

        val files = KotlinSourceScanner.scan(root, includeTests)
        val normalizedQuery = query.removePrefix(".")

        val matches = files.flatMap { scanned ->
            SymbolExtractor.extract(scanned.content, scanned.ktFile)
                .filter { symbol ->
                    val nameMatches =
                        if (exactMatch) symbol.name == normalizedQuery
                        else symbol.name.contains(normalizedQuery, ignoreCase = true)
                    nameMatches && (kind == null || symbol.kind == kind)
                }
                .map { symbol ->
                    Triple(symbol, scanned.relativePath, symbol.line)
                }
        }

        return buildJsonObject {
            put("status", "success")
            put("projectRoot", root.toString())
            put("scannedFileCount", files.size)
            put("matchCount", matches.size)

            if (matches.isEmpty()) {
                put(
                    "matches",
                    buildJsonArray {}
                )
            } else {
                put(
                    "matches",
                    buildJsonArray {
                        matches.take(maxResults).forEach { (symbol, relativePath, line) ->
                            add(
                                buildJsonObject {
                                    put("name", symbol.name)
                                    put("kind", symbol.kind)
                                    put("fqName", symbol.fqName)
                                    symbol.containerName?.let { put("container", it) }
                                    put("file", relativePath)
                                    put("line", JsonPrimitive(line))
                                }
                            )
                        }
                    }
                )
                put("matchesTruncated", matches.size > maxResults)
            }
        }
    }
}
