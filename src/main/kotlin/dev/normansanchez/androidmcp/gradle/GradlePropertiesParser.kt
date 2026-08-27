package dev.normansanchez.androidmcp.gradle

import java.nio.file.Files
import java.nio.file.Path

data class GradleConfig(
    val appliedPlugins: List<String>,
    val compileSdk: Int?,
    val minSdk: Int?,
    val targetSdk: Int?,
    val composeEnabled: Boolean,
    val buildTypes: List<String>,
    val productFlavors: List<String>,
    val javaVersion: String?,
    val namespace: String?,
    val kotlinVersion: String?,
    val androidGradlePluginVersion: String?
)

object GradlePropertiesParser {

    fun parse(moduleDir: Path): GradleConfig {
        val buildFile = findBuildFile(moduleDir) ?: return emptyConfig()
        val content = Files.readString(buildFile)
        return parseBuildContent(content, moduleDir)
    }

    fun parseBuildContent(content: String, moduleDir: Path): GradleConfig {
        val plugins = extractPlugins(content)
        val compileSdk = extractInt(content, "compileSdk")
        val minSdk = extractInt(content, "minSdk")
        val targetSdk = extractInt(content, "targetSdk")
        val composeEnabled = content.contains("compose = true")
        val buildTypes = extractBuildTypes(content)
        val productFlavors = extractProductFlavors(content)
        val javaVersion = extractJavaVersion(content)
        val namespace = extractString(content, "namespace")

        return GradleConfig(
            appliedPlugins = plugins,
            compileSdk = compileSdk,
            minSdk = minSdk,
            targetSdk = targetSdk,
            composeEnabled = composeEnabled,
            buildTypes = buildTypes,
            productFlavors = productFlavors,
            javaVersion = javaVersion,
            namespace = namespace,
            kotlinVersion = null,
            androidGradlePluginVersion = null
        )
    }

    fun enrichWithProjectProps(config: GradleConfig, projectRoot: Path): GradleConfig {
        val gradleProps = readGradleProperties(projectRoot)
        val versionCatalog = readVersionCatalogVersions(projectRoot)

        return config.copy(
            kotlinVersion = versionCatalog["kotlin"] ?: gradleProps["kotlin.version"],
            androidGradlePluginVersion = versionCatalog["agp"] ?: gradleProps["android.gradle.plugin.version"]
        )
    }

    private fun findBuildFile(moduleDir: Path): Path? {
        val kts = moduleDir.resolve("build.gradle.kts")
        if (Files.isRegularFile(kts)) return kts
        val groovy = moduleDir.resolve("build.gradle")
        if (Files.isRegularFile(groovy)) return groovy
        return null
    }

    private fun extractPlugins(content: String): List<String> {
        val plugins = mutableListOf<String>()

        Regex("""id\(["']([^"']+)["']\)""").findAll(content).forEach {
            plugins.add(it.groupValues[1])
        }

        Regex("""alias\(libs\.plugins\.([a-zA-Z0-9.-]+)\)""").findAll(content).forEach {
            plugins.add("alias:${it.groupValues[1]}")
        }

        return plugins.distinct()
    }

    private fun extractInt(content: String, key: String): Int? {
        val pattern = Regex("""$key\s*=\s*(\d+)""")
        return pattern.find(content)?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun extractString(content: String, key: String): String? {
        val pattern = Regex("""$key\s*=\s*"([^"]+)"""")
        return pattern.find(content)?.groupValues?.get(1)
    }

    private fun extractBuildTypes(content: String): List<String> {
        val types = mutableListOf<String>()
        Regex("""buildTypes\s*\{([\s\S]*?)\}""").find(content)?.let { match ->
            val block = match.groupValues[1]
            Regex("""(\w+)\s*\{""").findAll(block).forEach {
                types.add(it.groupValues[1])
            }
        }
        if (types.isEmpty() && content.contains("buildTypes")) {
            types.addAll(listOf("release", "debug"))
        }
        return types.ifEmpty { listOf("release", "debug") }
    }

    private fun extractProductFlavors(content: String): List<String> {
        val flavors = mutableListOf<String>()
        Regex("""productFlavors\s*\{([\s\S]*?)\}""").find(content)?.let { match ->
            val block = match.groupValues[1]
            Regex("""(\w+)\s*\{""").findAll(block).forEach {
                flavors.add(it.groupValues[1])
            }
        }
        return flavors
    }

    private fun extractJavaVersion(content: String): String? {
        val pattern = Regex("""JavaVersion\.VERSION_(\d+)""")
        return pattern.find(content)?.groupValues?.let { "1.${it[1]}" }
    }

    private fun readGradleProperties(projectRoot: Path): Map<String, String> {
        val propsFile = projectRoot.resolve("gradle.properties")
        if (!Files.isRegularFile(propsFile)) return emptyMap()

        return Files.readAllLines(propsFile)
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .associate { line ->
                val parts = line.split("=", limit = 2)
                if (parts.size == 2) parts[0].trim() to parts[1].trim() else "" to ""
            }
            .filter { it.key.isNotEmpty() }
    }

    private fun readVersionCatalogVersions(projectRoot: Path): Map<String, String> {
        val catalogFile = projectRoot.resolve("gradle/libs.versions.toml")
        if (!Files.isRegularFile(catalogFile)) return emptyMap()

        val content = Files.readString(catalogFile)
        val versions = mutableMapOf<String, String>()

        var inVersions = false
        for (line in content.lines()) {
            val trimmed = line.trim()
            when {
                trimmed == "[versions]" -> inVersions = true
                trimmed.startsWith("[") -> inVersions = false
                inVersions && trimmed.contains("=") -> {
                    val parts = trimmed.split("=", limit = 2)
                    if (parts.size == 2) {
                        versions[parts[0].trim()] = parts[1].trim().removeSurrounding("\"")
                    }
                }
            }
        }
        return versions
    }

    private fun emptyConfig() = GradleConfig(
        appliedPlugins = emptyList(),
        compileSdk = null,
        minSdk = null,
        targetSdk = null,
        composeEnabled = false,
        buildTypes = emptyList(),
        productFlavors = emptyList(),
        javaVersion = null,
        namespace = null,
        kotlinVersion = null,
        androidGradlePluginVersion = null
    )
}
