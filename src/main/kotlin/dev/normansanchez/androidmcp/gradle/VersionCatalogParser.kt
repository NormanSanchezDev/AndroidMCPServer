package dev.normansanchez.androidmcp.gradle

import java.nio.file.Files
import java.nio.file.Path

data class CatalogVersion(
    val name: String,
    val value: String
)

data class CatalogLibrary(
    val alias: String,
    val group: String,
    val name: String,
    val version: String?,
    val versionRef: String?
)

data class CatalogPlugin(
    val id: String,
    val version: String?,
    val versionRef: String?
)

data class VersionCatalog(
    val versions: List<CatalogVersion>,
    val libraries: List<CatalogLibrary>,
    val plugins: List<CatalogPlugin>
)

object VersionCatalogParser {

    fun parse(projectRoot: Path): VersionCatalog? {
        val catalogFile = projectRoot.resolve("gradle/libs.versions.toml")
        if (!Files.isRegularFile(catalogFile)) return null

        val content = Files.readString(catalogFile)
        return parseContent(content)
    }

    fun parseContent(content: String): VersionCatalog {
        val sections = splitSections(content)

        val versions = parseVersions(sections["versions"] ?: "")
        val libraries = parseLibraries(sections["libraries"] ?: "", versions)
        val plugins = parsePlugins(sections["plugins"] ?: "", versions)

        return VersionCatalog(
            versions = versions,
            libraries = libraries,
            plugins = plugins
        )
    }

    private fun splitSections(content: String): Map<String, String> {
        val sections = mutableMapOf<String, StringBuilder>()
        var currentSection = StringBuilder()
        var currentKey = ""

        for (line in content.lines()) {
            val trimmed = line.trim()
            val sectionMatch = Regex("""^\[(\w+)\]$""").matchEntire(trimmed)
            if (sectionMatch != null) {
                currentKey = sectionMatch.groupValues[1]
                currentSection = StringBuilder()
                sections[currentKey] = currentSection
            } else if (currentKey.isNotEmpty() && trimmed.isNotBlank() && !trimmed.startsWith("#")) {
                currentSection.appendLine(trimmed)
            }
        }

        return sections.mapValues { it.value.toString() }
    }

    private fun parseVersions(block: String): List<CatalogVersion> {
        return block.lines()
            .filter { it.contains("=") && !it.startsWith("#") }
            .mapNotNull { line ->
                val parts = line.split("=", limit = 2)
                if (parts.size == 2) {
                    CatalogVersion(
                        name = parts[0].trim(),
                        value = parts[1].trim().removeSurrounding("\"")
                    )
                } else null
            }
    }

    private fun parseLibraries(block: String, versions: List<CatalogVersion>): List<CatalogLibrary> {
        val versionMap = versions.associate { it.name to it.value }
        val libraries = mutableListOf<CatalogLibrary>()

        val entryPattern = Regex("""^(\w[\w.-]*)\s*=\s*\{(.+)\}$""")
        val groupPattern = Regex("""group\s*=\s*"([^"]+)"""")
        val namePattern = Regex("""name\s*=\s*"([^"]+)"""")
        val versionPattern = Regex("""version\s*=\s*"([^"]+)"""")
        val versionRefPattern = Regex("""version\.ref\s*=\s*"([^"]+)"""")

        for (line in block.lines()) {
            val trimmed = line.trim()
            val match = entryPattern.find(trimmed) ?: continue
            val alias = match.groupValues[1]
            val props = match.groupValues[2]

            val group = groupPattern.find(props)?.groupValues?.get(1) ?: continue
            val name = namePattern.find(props)?.groupValues?.get(1) ?: continue
            val version = versionPattern.find(props)?.groupValues?.get(1)
            val versionRef = versionRefPattern.find(props)?.groupValues?.get(1)

            val resolvedVersion = version ?: versionRef?.let { versionMap[it] }

            libraries.add(
                CatalogLibrary(
                    alias = alias,
                    group = group,
                    name = name,
                    version = resolvedVersion,
                    versionRef = versionRef
                )
            )
        }

        return libraries
    }

    private fun parsePlugins(block: String, versions: List<CatalogVersion>): List<CatalogPlugin> {
        val versionMap = versions.associate { it.name to it.value }
        val plugins = mutableListOf<CatalogPlugin>()

        val entryPattern = Regex("""^(\w[\w.-]*)\s*=\s*\{(.+)\}$""")
        val idPattern = Regex("""id\s*=\s*"([^"]+)"""")
        val versionPattern = Regex("""version\s*=\s*"([^"]+)"""")
        val versionRefPattern = Regex("""version\.ref\s*=\s*"([^"]+)"""")

        for (line in block.lines()) {
            val trimmed = line.trim()
            val match = entryPattern.find(trimmed) ?: continue
            val props = match.groupValues[2]

            val id = idPattern.find(props)?.groupValues?.get(1) ?: continue
            val version = versionPattern.find(props)?.groupValues?.get(1)
            val versionRef = versionRefPattern.find(props)?.groupValues?.get(1)

            val resolvedVersion = version ?: versionRef?.let { versionMap[it] }

            plugins.add(
                CatalogPlugin(
                    id = id,
                    version = resolvedVersion,
                    versionRef = versionRef
                )
            )
        }

        return plugins
    }
}
