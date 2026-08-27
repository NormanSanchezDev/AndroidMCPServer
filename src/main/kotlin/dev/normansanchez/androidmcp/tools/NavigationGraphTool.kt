package dev.normansanchez.androidmcp.tools

import dev.normansanchez.androidmcp.navigation.ComposeNavDetector
import dev.normansanchez.androidmcp.navigation.NavXmlParser
import dev.normansanchez.androidmcp.symbol.KotlinSourceScanner
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name

object NavigationGraphTool {

    fun execute(projectRoot: String): kotlinx.serialization.json.JsonObject {
        val root = Path.of(projectRoot).normalize().toAbsolutePath()

        if (!Files.isDirectory(root)) {
            return buildJsonObject {
                put("status", "invalid_project")
                put("projectRoot", root.toString())
            }
        }

        val xmlGraphs = parseXmlNavigation(root)
        val scannedFiles = KotlinSourceScanner.scan(root, includeTests = false)
        val composeRoutes = ComposeNavDetector.detect(scannedFiles)
        val hasNavHost = ComposeNavDetector.hasNavHost(scannedFiles)

        return buildJsonObject {
            put("status", "success")
            put("projectRoot", root.toString())
            put("hasXmlNavigation", xmlGraphs.isNotEmpty())
            put("hasComposeNavigation", hasNavHost)
            put(
                "xmlGraphs",
                buildJsonArray {
                    xmlGraphs.forEach { (path, graph) ->
                        add(buildJsonObject {
                            put("file", path)
                            put("startDestination", graph.startDestination?.let { JsonPrimitive(it) } ?: JsonPrimitive(null as String?))
                            put("destinationCount", graph.destinations.size)
                            put(
                                "destinations",
                                buildJsonArray {
                                    graph.destinations.forEach { dest ->
                                        add(buildJsonObject {
                                            put("id", dest.id)
                                            put("label", dest.label?.let { JsonPrimitive(it) } ?: JsonPrimitive(null as String?))
                                            put("isStartDestination", dest.isStartDestination)
                                            put(
                                                "deepLinks",
                                                buildJsonArray {
                                                    dest.deepLinks.forEach { add(JsonPrimitive(it)) }
                                                }
                                            )
                                        })
                                    }
                                }
                            )
                        })
                    }
                }
            )
            put(
                "composeRoutes",
                buildJsonArray {
                    composeRoutes.forEach { route ->
                        add(buildJsonObject {
                            put("route", route.route)
                            put("file", route.file)
                            put("line", route.line)
                        })
                    }
                }
            )
        }
    }

    private fun parseXmlNavigation(root: Path): Map<String, dev.normansanchez.androidmcp.navigation.NavGraph> {
        val graphs = mutableMapOf<String, dev.normansanchez.androidmcp.navigation.NavGraph>()

        Files.walk(root, 8).use { paths ->
            paths.filter { Files.isRegularFile(it) && it.name.endsWith(".xml") }
                .filter { it.toString().contains("navigation") }
                .forEach { file ->
                    val content = try { Files.readString(file) } catch (_: Exception) { return@forEach }
                    if (content.contains("<nav-graph") || content.contains("android:name=")) {
                        try {
                            val graph = NavXmlParser.parse(content)
                            if (graph.destinations.isNotEmpty()) {
                                graphs[root.relativize(file).toString()] = graph
                            }
                        } catch (_: Exception) {}
                    }
                }
        }

        return graphs
    }
}
