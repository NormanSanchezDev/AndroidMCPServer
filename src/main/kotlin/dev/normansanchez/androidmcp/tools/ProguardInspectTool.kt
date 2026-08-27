package dev.normansanchez.androidmcp.tools

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.nio.file.Files
import java.nio.file.Path

object ProguardInspectTool {

    data class ProguardRule(
        val type: String,
        val pattern: String,
        val lineNumber: Int,
        val rawLine: String
    )

    data class ProguardFile(
        val path: String,
        val rules: List<ProguardRule>
    )

    fun execute(projectRoot: String): kotlinx.serialization.json.JsonObject {
        val root = Path.of(projectRoot).normalize().toAbsolutePath()

        if (!Files.isDirectory(root)) {
            return buildJsonObject {
                put("status", "invalid_project")
                put("projectRoot", root.toString())
            }
        }

        val proguardFiles = findProguardFiles(root)
        if (proguardFiles.isEmpty()) {
            return buildJsonObject {
                put("status", "not_available")
                put("projectRoot", root.toString())
                put("error", "No ProGuard/R8 rule files found")
            }
        }

        return buildJsonObject {
            put("status", "success")
            put("projectRoot", root.toString())
            put("fileCount", proguardFiles.size)
            put(
                "files",
                buildJsonArray {
                    proguardFiles.forEach { file ->
                        add(buildJsonObject {
                            put("file", file.path)
                            put("ruleCount", file.rules.size)
                            put(
                                "rules",
                                buildJsonArray {
                                    file.rules.forEach { rule ->
                                        add(buildJsonObject {
                                            put("type", rule.type)
                                            put("pattern", rule.pattern)
                                            put("line", rule.lineNumber)
                                        })
                                    }
                                }
                            )
                        })
                    }
                }
            )
        }
    }

    private fun findProguardFiles(root: Path): List<ProguardFile> {
        val files = mutableListOf<ProguardFile>()

        Files.walk(root, 8).use { paths ->
            paths.filter { Files.isRegularFile(it) }
                .filter { file ->
                    val name = file.fileName.toString().lowercase()
                    name.endsWith(".pro") ||
                            name.startsWith("proguard") ||
                            name.startsWith("consumer-rules")
                }
                .forEach { file ->
                    val content = try { Files.readString(file) } catch (_: Exception) { return@forEach }
                    val rules = parseRules(content)
                    if (rules.isNotEmpty()) {
                        files.add(
                            ProguardFile(
                                path = root.relativize(file).toString(),
                                rules = rules
                            )
                        )
                    }
                }
        }

        val buildGradleFiles = mutableListOf<Path>()
        Files.walk(root, 3).use { paths ->
            paths.filter { Files.isRegularFile(it) }
                .filter { it.fileName.toString() == "build.gradle.kts" || it.fileName.toString() == "build.gradle" }
                .forEach { buildGradleFiles.add(it) }
        }

        for (gradleFile in buildGradleFiles) {
            val content = try { Files.readString(gradleFile) } catch (_: Exception) { continue }
            if (content.contains("proguard") || content.contains("Proguard") || content.contains("minifyEnabled")) {
                val rules = extractProguardConfig(content)
                if (rules.isNotEmpty()) {
                    val path = root.relativize(gradleFile).toString()
                    if (files.none { it.path == path }) {
                        files.add(ProguardFile(path = path, rules = rules))
                    }
                }
            }
        }

        return files
    }

    private fun parseRules(content: String): List<ProguardRule> {
        val rules = mutableListOf<ProguardRule>()

        val ruleTypes = mapOf(
            Regex("""^-keep\s+class\s+(.+)\s*\{""") to "keep_class",
            Regex("""^-keep\s+class\s+(.+)""") to "keep_class",
            Regex("""^-keepclassmembers\s+class\s+(.+)\s*\{""") to "keepclassmembers",
            Regex("""^-keepclassmembers\s+(.+)""") to "keepclassmembers",
            Regex("""^-keepnames\s+class\s+(.+)""") to "keepnames",
            Regex("""^-keepclassmembernames\s+(.+)""") to "keepclassmembernames",
            Regex("""^-dontwarn\s+(.+)""") to "dontwarn",
            Regex("""^-dontnote\s+(.+)""") to "dontnote",
            Regex("""^-assumenosideeffects\s+(.+)""") to "assumenosideeffects",
            Regex("""^-print(seeds|usage|mapping)\s+(.+)""") to "print",
            Regex("""^-renamesourcefileattribute\s+(.+)""") to "renamesourcefileattribute",
            Regex("""^-repackageclasses\s+(.+)""") to "repackageclasses",
            Regex("""^-allowaccessmodification""") to "allowaccessmodification",
            Regex("""^-optimizationpasses\s+(\d+)""") to "optimizationpasses",
            Regex("""^-keep\s+\*""") to "keep_wildcard",
            Regex("""^-keep\s+#\s*(.+)""") to "keep_with_comment",
            Regex("""^-include\s+(.+)""") to "include",
            Regex("""^-applymapping\s+(.+)""") to "applymapping",
            Regex("""^-obfuscationdictionary\s+(.+)""") to "obfuscationdictionary",
            Regex("""^-classobfuscationdictionary\s+(.+)""") to "classobfuscationdictionary",
            Regex("""^-overloadaggressively""") to "overloadaggressively",
            Regex("""^-useuniqueclassmembernames""") to "useuniqueclassmembernames",
            Regex("""^-flattenpackagehierarchy\s+(.+)""") to "flattenpackagehierarchy",
            Regex("""^-mergeinterfacesaggressively""") to "mergeinterfacesaggressively",
        )

        val lines = content.lines()
        for ((index, line) in lines.withIndex()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue

            for ((pattern, type) in ruleTypes) {
                val match = pattern.find(trimmed)
                if (match != null) {
                    rules.add(
                        ProguardRule(
                            type = type,
                            pattern = match.groupValues.getOrElse(1) { trimmed }.trim(),
                            lineNumber = index + 1,
                            rawLine = trimmed
                        )
                    )
                    break
                }
            }
        }

        return rules
    }

    private fun extractProguardConfig(gradleContent: String): List<ProguardRule> {
        val rules = mutableListOf<ProguardRule>()

        if (gradleContent.contains("isMinifyEnabled = true") || gradleContent.contains("minifyEnabled true")) {
            rules.add(
                ProguardRule(
                    type = "minification_enabled",
                    pattern = "enabled in build.gradle",
                    lineNumber = 0,
                    rawLine = "minification enabled"
                )
            )
        }

        if (gradleContent.contains("isShrinkResources = true") || gradleContent.contains("shrinkResources true")) {
            rules.add(
                ProguardRule(
                    type = "resource_shrinking_enabled",
                    pattern = "enabled in build.gradle",
                    lineNumber = 0,
                    rawLine = "resource shrinking enabled"
                )
            )
        }

        val proguardFilePattern = Regex("""proguardFiles\(.*?"([^"]+)".*\)""")
        proguardFilePattern.findAll(gradleContent).forEach { match ->
            rules.add(
                ProguardRule(
                    type = "proguard_file_ref",
                    pattern = match.groupValues[1],
                    lineNumber = 0,
                    rawLine = "proguardFiles(${match.groupValues[1]})"
                )
            )
        }

        val consumerRulePattern = Regex("""consumerProguardFiles\(.*?"([^"]+)".*\)""")
        consumerRulePattern.findAll(gradleContent).forEach { match ->
            rules.add(
                ProguardRule(
                    type = "consumer_proguard_file_ref",
                    pattern = match.groupValues[1],
                    lineNumber = 0,
                    rawLine = "consumerProguardFiles(${match.groupValues[1]})"
                )
            )
        }

        return rules
    }
}
