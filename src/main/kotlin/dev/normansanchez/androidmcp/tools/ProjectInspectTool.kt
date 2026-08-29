package dev.normansanchez.androidmcp.tools

import dev.normansanchez.androidmcp.model.AndroidModuleEvidence
import dev.normansanchez.androidmcp.model.AndroidModuleType
import dev.normansanchez.androidmcp.util.isUnderExcludedDir
import kotlinx.serialization.json.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile

object ProjectInspectTool {
    fun execute(projectRoot: String): JsonObject {
        val root = Path.of(projectRoot)
            .normalize()
            .toAbsolutePath()

        val exists = Files.exists(root)
        val isDirectory = Files.isDirectory(root)

        if (!exists || !isDirectory) {
            return buildJsonObject {
                put("status", "invalid_project")
                put("projectRoot", root.toString())
                put("exists", exists)
                put("isDirectory", isDirectory)
            }
        }

        val settingsFile = findSettingsFile(root)
        val modules = findModules(root)
        val androidModules = modules.filter {
            it.type == AndroidModuleType.APPLICATION ||
                    it.type == AndroidModuleType.LIBRARY
        }

        return buildJsonObject {
            put("status", "success")
            put("projectRoot", root.toString())
            put("gradleProject", settingsFile != null)
            put(
                "settingsFile",
                settingsFile?.let {
                    JsonPrimitive(
                        root.relativize(it).toString()
                    )
                } ?: JsonPrimitive(null)
            )

            put("androidProject", androidModules.isNotEmpty())
            put(
                "modules",
                buildJsonArray {
                    modules.forEach { module ->
                        add(
                            buildJsonObject {
                                put("name", module.name)
                                put("path", module.path)
                                put("type", module.type.value)
                                put("buildFile", module.buildFile)
                                put(
                                    "manifest",
                                    module.manifest?.let {
                                        JsonPrimitive(it)
                                    } ?: JsonPrimitive(null)
                                )
                            }
                        )
                    }
                }
            )
            put(
                "evidence",
                buildEvidence(androidModules)
            )
        }
    }

    private fun findSettingsFile(root: Path): Path? {
        val kotlinDsl = root.resolve("settings.gradle.kts")
        if (Files.isRegularFile(kotlinDsl)) {
            return kotlinDsl
        }

        val groovyDsl = root.resolve("settings.gradle")
        if (Files.isRegularFile(groovyDsl)) {
            return groovyDsl
        }
        return null

    }

    private fun findModules(root: Path): List<AndroidModuleEvidence> {
        return Files.walk(root, MAX_MODULE_DEPTH).use { paths ->
            paths
                .filter { path ->
                    path.isRegularFile() && (path.fileName.toString() == "build.gradle.kts" || path.fileName.toString() == "build.gradle")
                }
                .filter { path -> !path.isUnderExcludedDir(root, PROJECT_INFRA_DIRS) }
                .map { buildFile ->
                    inspectModule(
                        root = root,
                        buildFile = buildFile
                    )
                }
                .filter { module ->
                    // The root build file only counts when it is itself an Android module;
                    // modules declared with `apply false` are infra, not modules.
                    module.path != "." || module.type != AndroidModuleType.UNKNOWN
                }
                .toList()

        }

    }

    private fun inspectModule(
        root: Path,
        buildFile: Path
    ): AndroidModuleEvidence {
        val moduleRoot = buildFile.parent
        val content = Files.readString(buildFile)
        val type = when {
            appliesPlugin(content, APPLICATION_PLUGIN_MARKERS) ->
                AndroidModuleType.APPLICATION
            appliesPlugin(content, LIBRARY_PLUGIN_MARKERS) ->
                AndroidModuleType.LIBRARY
            else -> AndroidModuleType.UNKNOWN
        }

        val manifest = moduleRoot
            .resolve("src/main/AndroidManifest.xml")
            .takeIf(Files::isRegularFile)

        val isRootModule = moduleRoot == root
        return AndroidModuleEvidence(
            name = if (isRootModule) (root.fileName?.toString() ?: ".") else (moduleRoot.fileName?.toString() ?: "."),
            path = if (isRootModule) "." else root.relativize(moduleRoot).toString(),
            type = type,
            buildFile = root.relativize(buildFile).toString(),
            manifest = manifest?.let { root.relativize(it).toString() }
        )
    }

    private fun appliesPlugin(
        content: String,
        markers: List<String>
    ): Boolean {
        return content.lines().any { line ->
            notApplyFalse(line) && markers.any { line.contains(it) }
        }
    }

    private fun notApplyFalse(line: String): Boolean {
        val trimmed = line.trim()
        return !trimmed.endsWith("apply false") && !trimmed.endsWith("apply false)") &&
                !trimmed.contains(" apply false ")
    }

    private val APPLICATION_PLUGIN_MARKERS = listOf(
        """id("com.android.application")""",
        "id 'com.android.application'",
        "alias(libs.plugins.android.application)"
    )

    private val LIBRARY_PLUGIN_MARKERS = listOf(
        """id("com.android.library")""",
        "id 'com.android.library'",
        "alias(libs.plugins.android.library)"
    )

    private val PROJECT_INFRA_DIRS = setOf("build-logic", "buildSrc")

    private const val MAX_MODULE_DEPTH = 6

    private fun buildEvidence(
        modules: List<AndroidModuleEvidence>
    ): JsonArray {

        return buildJsonArray {
            modules.forEach { module ->
                add(
                    buildJsonObject {
                        put("type", "android_gradle_plugin")
                        put("module", module.name)
                        put("file", module.buildFile)
                        put("value", module.type.pluginId)
                    }
                )
                module.manifest?.let { manifest ->
                    add(
                        buildJsonObject {
                            put("type", "android_manifest")
                            put("module", module.name)
                            put("file", manifest)
                        }
                    )
                }
            }
        }
    }
}