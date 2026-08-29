package dev.normansanchez.androidmcp.flow

import java.nio.file.Files
import java.nio.file.Path

data class SourceFileRef(
    val module: FlowModuleInfo,
    val path: Path,
    val relativePath: String,
    val content: String
)

object SourceScanner {

    private val excludedDirectories = setOf("build", ".gradle", "kotlin")

    fun scanSources(project: AndroidProjectScan): List<SourceFileRef> {
        val files = mutableListOf<SourceFileRef>()
        for (module in project.modules) {
            val src = module.path.resolve("src/main")
            if (!Files.isDirectory(src)) continue
            Files.walk(src).use { paths ->
                paths.forEach { path ->
                    if (Files.isRegularFile(path) && path.isSourceFile()) {
                        if (path.anyParentIn(excludedDirectories)) return@forEach
                        try {
                            files.add(
                                SourceFileRef(
                                    module = module,
                                    path = path,
                                    relativePath = project.root.relativize(path).toString(),
                                    content = Files.readString(path)
                                )
                            )
                        } catch (_: Exception) {
                        }
                    }
                }
            }
        }
        return files
    }

    fun scanNavigationXml(project: AndroidProjectScan): List<NavXmlGraph> {
        val graphs = mutableListOf<NavXmlGraph>()
        for (module in project.modules) {
            val navigationDir = module.path.resolve("src/main/res/navigation")
            if (!Files.isDirectory(navigationDir)) continue
            Files.walk(navigationDir).use { paths ->
                paths.forEach { path ->
                    if (Files.isRegularFile(path) && path.toString().endsWith(".xml")) {
                        XmlNavGraphParser.parseFile(path, project.root)?.let { graphs.add(it) }
                    }
                }
            }
        }
        return graphs
    }

    fun scanLayoutImages(project: AndroidProjectScan): List<Pair<Path, String>> {
        val layouts = mutableListOf<Pair<Path, String>>()
        for (module in project.modules) {
            val resDir = module.path.resolve("src/main/res")
            if (!Files.isDirectory(resDir)) continue
            Files.walk(resDir).use { paths ->
                paths.forEach { path ->
                    if (Files.isRegularFile(path) && path.toString().endsWith(".xml")) {
                        try {
                            layouts.add(path to Files.readString(path))
                        } catch (_: Exception) {
                        }
                    }
                }
            }
        }
        return layouts
    }

    fun screenNameOfFile(content: String): String? {
        val composable = Regex("""@Composable[\s\S]{0,120}?fun\s+([A-Za-z_][A-Za-z0-9_]*)""")
            .find(content)
        if (composable != null) return composable.groupValues[1]
        val clazz = Regex("""(?:^|\s)(?:abstract\s+|open\s+|final\s+)?class\s+([A-Za-z_][A-Za-z0-9_]*)""")
            .find(content)
        return clazz?.groupValues?.get(1)
    }

    fun lineOf(content: String, offset: Int): Int =
        content.substring(0, offset.coerceAtMost(content.length)).count { it == '\n' } + 1

    fun simpleName(qualified: String): String =
        qualified.substringAfterLast('.').takeIf { it.isNotBlank() } ?: qualified

    private fun Path.isSourceFile(): Boolean =
        fileName.toString().endsWith(".kt") || fileName.toString().endsWith(".java")

    private fun Path.anyParentIn(names: Set<String>): Boolean {
        var parent: Path? = parent
        while (parent != null) {
            if (parent.fileName?.toString() in names) return true
            parent = parent.parent
        }
        return false
    }
}