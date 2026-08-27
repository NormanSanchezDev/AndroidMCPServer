package dev.normansanchez.androidmcp.gradle

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension

data class ConventionPlugin(
    val id: String,
    val className: String,
    val filePath: String,
    val appliedBy: List<String>
)

object ConventionPluginScanner {

    fun scan(projectRoot: Path): List<ConventionPlugin> {
        val plugins = mutableListOf<ConventionPlugin>()

        val buildLogicDir = projectRoot.resolve("build-logic")
        val buildSrcDir = projectRoot.resolve("buildSrc")

        if (Files.isDirectory(buildLogicDir)) {
            plugins.addAll(scanDirectory(buildLogicDir, projectRoot))
        }
        if (Files.isDirectory(buildSrcDir)) {
            plugins.addAll(scanDirectory(buildSrcDir, projectRoot))
        }

        val modulePlugins = mapPluginsToModules(plugins, projectRoot)
        return modulePlugins
    }

    private fun scanDirectory(dir: Path, projectRoot: Path): List<ConventionPlugin> {
        val plugins = mutableListOf<ConventionPlugin>()
        val kotlinSrc = dir.resolve("src/main/kotlin")

        if (!Files.isDirectory(kotlinSrc)) return plugins

        Files.walk(kotlinSrc).use { paths ->
            paths.filter { Files.isRegularFile(it) && it.extension == "kt" }
                .forEach { file ->
                    val content = try { Files.readString(file) } catch (_: Exception) { return@forEach }

                    val pluginId = extractPluginId(content, dir)
                    val className = extractClassName(content)

                    if (pluginId != null && className != null) {
                        plugins.add(
                            ConventionPlugin(
                                id = pluginId,
                                className = className,
                                filePath = projectRoot.relativize(file).toString(),
                                appliedBy = emptyList()
                            )
                        )
                    }
                }
        }

        val metaInfDir = dir.resolve("src/main/resources/META-INF/gradle-plugins")
        if (Files.isDirectory(metaInfDir)) {
            Files.walk(metaInfDir).use { paths ->
                paths.filter { Files.isRegularFile(it) && it.extension == "properties" }
                    .forEach { file ->
                        val propsContent = try { Files.readString(file) } catch (_: Exception) { return@forEach }
                        val implementationClass = Regex("""implementation-class=(.+)""").find(propsContent)
                            ?.groupValues?.get(1)?.trim()
                        val pluginId = file.fileName.toString().removeSuffix(".properties")

                        if (implementationClass != null) {
                            val existing = plugins.find { it.id == pluginId }
                            if (existing == null) {
                                plugins.add(
                                    ConventionPlugin(
                                        id = pluginId,
                                        className = implementationClass,
                                        filePath = projectRoot.relativize(file).toString(),
                                        appliedBy = emptyList()
                                    )
                                )
                            }
                        }
                    }
            }
        }

        return plugins
    }

    private fun extractPluginId(content: String, dir: Path): String? {
        val parentDirName = dir.parent?.fileName?.toString() ?: return null
        val propsFile = dir.resolve("src/main/resources/META-INF/gradle-plugins/$parentDirName.properties")
        if (Files.isRegularFile(propsFile)) {
            return parentDirName
        }

        val pluginIdPattern = Regex("""pluginId\s*=\s*"([^"]+)"""")
        return pluginIdPattern.find(content)?.groupValues?.get(1)
    }

    private fun extractClassName(content: String): String? {
        val classPattern = Regex("""class\s+(\w+)""")
        return classPattern.find(content)?.groupValues?.get(1)
    }

    private fun mapPluginsToModules(plugins: List<ConventionPlugin>, projectRoot: Path): List<ConventionPlugin> {
        val moduleAppliedPlugins = mutableMapOf<String, MutableList<String>>()

        Files.walk(projectRoot, 3).use { paths ->
            paths.filter { Files.isRegularFile(it) }
                .filter { it.fileName.toString() == "build.gradle.kts" || it.fileName.toString() == "build.gradle" }
                .filter { it.parent != projectRoot }
                .forEach { buildFile ->
                    val content = try { Files.readString(buildFile) } catch (_: Exception) { return@forEach }
                    val moduleName = projectRoot.relativize(buildFile.parent).toString()

                    plugins.forEach { plugin ->
                        if (content.contains(plugin.id) || content.contains("alias(libs.plugins.${plugin.id.substringAfterLast(".")})")) {
                            moduleAppliedPlugins.getOrPut(plugin.id) { mutableListOf() }.add(moduleName)
                        }
                    }
                }
        }

        return plugins.map { plugin ->
            plugin.copy(appliedBy = moduleAppliedPlugins[plugin.id] ?: emptyList())
        }
    }
}
