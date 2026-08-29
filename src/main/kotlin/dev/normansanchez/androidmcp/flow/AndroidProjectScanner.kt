package dev.normansanchez.androidmcp.flow

import dev.normansanchez.androidmcp.gradle.GradlePropertiesParser
import dev.normansanchez.androidmcp.graph.ModuleGraphParser
import java.nio.file.Files
import java.nio.file.Path

data class FlowModuleInfo(
    val name: String,
    val path: Path,
    val type: String,
    val namespace: String?,
    val manifest: Path?
)

data class AndroidProjectScan(
    val root: Path,
    val rootProjectName: String?,
    val modules: List<FlowModuleInfo>,
    val moduleDependencies: List<ModuleDependency>,
    val appModules: List<FlowModuleInfo>
)

object AndroidProjectScanner {

    private val applicationPluginPattern =
        Regex("""(?:id\s*\(\s*["']com\.android\.application["']\s*\)|alias\s*\(\s*libs\.plugins\.android\.application\s*\))""")
    private val libraryPluginPattern =
        Regex("""(?:id\s*\(\s*["']com\.android\.library["']\s*\)|alias\s*\(\s*libs\.plugins\.android\.library\s*\))""")

    fun scan(root: Path): AndroidProjectScan? {
        val normalized = root.normalize().toAbsolutePath()
        val settings = findSettings(normalized)
        val settingsModules = settings?.let {
            ModuleGraphParser.parseSettings(Files.readString(it)).second
        } ?: emptyList()

        val modules = mutableListOf<FlowModuleInfo>()
        settingsModules.forEach { name ->
            val dir = normalized.resolve(name)
            if (Files.isDirectory(dir)) {
                modules.add(moduleInfo(normalized, name, dir))
            }
        }
        if (modules.isEmpty()) {
            findBuildFile(normalized)?.let {
                modules.add(moduleInfo(normalized, "root", normalized))
            }
        }
        if (modules.isEmpty()) return null

        val dependencies = modules.flatMap { module ->
            val buildFile = findBuildFile(module.path) ?: return@flatMap emptyList()
            ModuleGraphParser.parseBuildFile(module.name, Files.readString(buildFile)).map {
                ModuleDependency(it.from, it.to, it.configuration)
            }
        }
        val appModules = modules.filter { it.type == TYPE_APPLICATION }

        return AndroidProjectScan(
            root = normalized,
            rootProjectName = settings?.let {
                ModuleGraphParser.parseSettings(Files.readString(it)).first
            },
            modules = modules,
            moduleDependencies = dependencies,
            appModules = appModules
        )
    }

    private fun moduleInfo(root: Path, name: String, dir: Path): FlowModuleInfo =
        FlowModuleInfo(
            name = name,
            path = dir,
            type = classifyModule(dir),
            namespace = GradlePropertiesParser.parse(dir).namespace,
            manifest = dir.resolve("src/main/AndroidManifest.xml").takeIf { Files.isRegularFile(it) }
        )

    private fun classifyModule(moduleDir: Path): String {
        val buildFile = findBuildFile(moduleDir) ?: return TYPE_UNKNOWN
        val content = Files.readString(buildFile)
        return when {
            applicationPluginPattern.containsMatchIn(content) -> TYPE_APPLICATION
            libraryPluginPattern.containsMatchIn(content) -> TYPE_LIBRARY
            else -> TYPE_UNKNOWN
        }
    }

    private fun findSettings(root: Path): Path? =
        listOf("settings.gradle.kts", "settings.gradle")
            .map { root.resolve(it) }
            .firstOrNull { Files.isRegularFile(it) }

    private fun findBuildFile(moduleDir: Path): Path? =
        listOf("build.gradle.kts", "build.gradle")
            .map { moduleDir.resolve(it) }
            .firstOrNull { Files.isRegularFile(it) }

    const val TYPE_APPLICATION = "APPLICATION"
    const val TYPE_LIBRARY = "LIBRARY"
    const val TYPE_UNKNOWN = "UNKNOWN"
}