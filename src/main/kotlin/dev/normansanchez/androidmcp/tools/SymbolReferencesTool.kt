package dev.normansanchez.androidmcp.tools

import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import dev.normansanchez.androidmcp.symbol.KotlinPsiEngine
import dev.normansanchez.androidmcp.symbol.KotlinSourceScanner
import dev.normansanchez.androidmcp.symbol.SymbolExtractor

object SymbolReferencesTool {

    fun execute(
        projectRoot: String,
        symbolName: String,
        includeTests: Boolean = false,
        maxResults: Int = 200
    ): kotlinx.serialization.json.JsonObject {
        val root = Path.of(projectRoot).normalize().toAbsolutePath()

        if (!Files.isDirectory(root)) {
            return buildJsonObject {
                put("status", "invalid_project")
                put("projectRoot", root.toString())
            }
        }

        val identifierRegex = Regex("^[A-Za-z_][A-Za-z0-9_]*$")
        if (!identifierRegex.matches(symbolName)) {
            return buildJsonObject {
                put("status", "invalid_request")
                put("error", "symbolName must be a plain Kotlin identifier")
            }
        }

        val files = KotlinSourceScanner.scan(root, includeTests)

        data class Occurrence(
            val file: String,
            val line: Int,
            val isDeclaration: Boolean,
            val container: String?
        )

        val declarations = mutableListOf<Occurrence>()
        val references = mutableListOf<Occurrence>()
        val occurrenceRegex = Regex("\\b${Regex.escape(symbolName)}\\b")

        for (scanned in files) {
            val symbolsInFile =
                SymbolExtractor.extract(scanned.content, scanned.ktFile)
                    .filter { it.name == symbolName }

            val declarationOffsets = symbolsInFile.map { it.nameOffset }.toSet()

            symbolsInFile.forEach { symbol ->
                declarations.add(
                    Occurrence(
                        file = scanned.relativePath,
                        line = symbol.line,
                        isDeclaration = true,
                        container = symbol.containerName
                    )
                )
            }

            occurrenceRegex.findAll(scanned.content).forEach { match ->
                if (match.range.first in declarationOffsets) {
                    return@forEach
                }
                references.add(
                    Occurrence(
                        file = scanned.relativePath,
                        line = KotlinPsiEngine.lineOf(scanned.content, match.range.first),
                        isDeclaration = false,
                        container = null
                    )
                )
            }
        }

        return buildJsonObject {
            put("status", "success")
            put("projectRoot", root.toString())
            put("symbol", symbolName)
            put("scannedFileCount", files.size)
            put("declarationCount", declarations.size)
            put("referenceCount", references.size)

            put(
                "declarations",
                buildJsonArray {
                    declarations.forEach { occurrence ->
                        add(
                            buildJsonObject {
                                put("file", occurrence.file)
                                put("line", occurrence.line)
                                occurrence.container?.let { put("container", it) }
                            }
                        )
                    }
                }
            )

            put(
                "references",
                buildJsonArray {
                    references.take(maxResults).forEach { occurrence ->
                        add(
                            buildJsonObject {
                                put("file", occurrence.file)
                                put("line", occurrence.line)
                            }
                        )
                    }
                }
            )
            put("referencesTruncated", references.size > maxResults)

            put(
                "limitation",
                "Identifier-level analysis within the module source sets; " +
                        "occurrences with the same name are reported even when they refer to different types."
            )
        }
    }
}
