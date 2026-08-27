package dev.normansanchez.androidmcp.tools

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name

object SecurityAuditTool {

    data class SecurityIssue(
        val severity: String,
        val category: String,
        val message: String,
        val file: String,
        val line: Int
    )

    fun execute(projectRoot: String): kotlinx.serialization.json.JsonObject {
        val root = Path.of(projectRoot).normalize().toAbsolutePath()

        if (!Files.isDirectory(root)) {
            return buildJsonObject {
                put("status", "invalid_project")
                put("projectRoot", root.toString())
            }
        }

        val issues = mutableListOf<SecurityIssue>()

        issues.addAll(checkManifest(root))
        issues.addAll(checkGradleProperties(root))
        issues.addAll(checkHardcodedSecrets(root))

        val bySeverity = issues.groupBy { it.severity }

        return buildJsonObject {
            put("status", "success")
            put("projectRoot", root.toString())
            put("totalIssues", issues.size)
            put("criticalCount", bySeverity["critical"]?.size ?: 0)
            put("warningCount", bySeverity["warning"]?.size ?: 0)
            put("infoCount", bySeverity["info"]?.size ?: 0)
            put(
                "issues",
                buildJsonArray {
                    issues.forEach { issue ->
                        add(buildJsonObject {
                            put("severity", issue.severity)
                            put("category", issue.category)
                            put("message", issue.message)
                            put("file", issue.file)
                            put("line", issue.line)
                        })
                    }
                }
            )
        }
    }

    private val exportableTagRegex = Regex("""<(activity|service|receiver|provider)\b""")

    private fun checkManifest(root: Path): List<SecurityIssue> {
        val issues = mutableListOf<SecurityIssue>()

        Files.walk(root, 8).use { paths ->
            paths.filter { Files.isRegularFile(it) && it.name == "AndroidManifest.xml" }
                .forEach { file ->
                    val content = try { Files.readString(file) } catch (_: Exception) { return@forEach }
                    val lines = content.lines()
                    val fileStr = root.relativize(file).toString()

                    for ((index, line) in lines.withIndex()) {
                        if (line.contains("""android:exported="true"""")) {
                            // intent-filter is a child element, so it lives on lines AFTER the
                            // opening tag, not on this line. Scan the enclosing element's block
                            // instead of just this line, or every exported component is flagged.
                            val tagStart = (index downTo 0).firstOrNull { exportableTagRegex.containsMatchIn(lines[it]) } ?: index
                            val tagEnd = (tagStart until lines.size).firstOrNull {
                                lines[it].contains("/>") || lines[it].trimStart().startsWith("</")
                            } ?: (lines.size - 1)
                            val block = lines.subList(tagStart, (tagEnd + 1).coerceAtMost(lines.size)).joinToString("\n")
                            if (!block.contains("intent-filter")) {
                                issues.add(SecurityIssue("warning", "exported", "Component exported without intent-filter", fileStr, index + 1))
                            }
                        }
                        if (line.contains("""android:allowBackup="true"""")) {
                            issues.add(SecurityIssue("info", "backup", "allowBackup is enabled", fileStr, index + 1))
                        }
                        if (line.contains("""android:usesCleartextTraffic="true"""")) {
                            issues.add(SecurityIssue("warning", "cleartext", "Cleartext traffic is allowed", fileStr, index + 1))
                        }
                    }
                }
        }

        return issues
    }

    private fun checkGradleProperties(root: Path): List<SecurityIssue> {
        val issues = mutableListOf<SecurityIssue>()

        val propsFiles = listOf(
            root.resolve("gradle.properties"),
            root.resolve("local.properties")
        )

        for (file in propsFiles) {
            if (!Files.isRegularFile(file)) continue
            val content = try { Files.readString(file) } catch (_: Exception) { continue }
            val fileStr = root.relativize(file).toString()
            val lines = content.lines()

            val sensitiveKeys = listOf("password", "token", "secret", "key", "api")

            for ((index, line) in lines.withIndex()) {
                if (line.startsWith("#") || !line.contains("=")) continue
                val key = line.split("=", limit = 2)[0].trim().lowercase()
                if (sensitiveKeys.any { key.contains(it) }) {
                    issues.add(SecurityIssue("critical", "secret", "Potential secret in $fileStr: ${line.split("=", limit = 2)[0].trim()}", fileStr, index + 1))
                }
            }
        }

        return issues
    }

    private fun checkHardcodedSecrets(root: Path): List<SecurityIssue> {
        val issues = mutableListOf<SecurityIssue>()

        val secretPatterns = listOf(
            Regex("""(?:api[_-]?key|secret|password|token)\s*=\s*"[A-Za-z0-9+/=]{8,}"""", RegexOption.IGNORE_CASE),
            Regex("""(?:AWS|AKIA)[A-Z0-9]{16}""")
        )

        Files.walk(root, 8).use { paths ->
            paths.filter { Files.isRegularFile(it) }
                .filter { it.name.endsWith(".kt") || it.name.endsWith(".java") || it.name.endsWith(".xml") }
                .forEach { file ->
                    val content = try { Files.readString(file) } catch (_: Exception) { return@forEach }
                    val fileStr = root.relativize(file).toString()
                    val lines = content.lines()

                    for ((index, line) in lines.withIndex()) {
                        for (pattern in secretPatterns) {
                            if (pattern.containsMatchIn(line)) {
                                issues.add(SecurityIssue("critical", "hardcoded_secret", "Potential hardcoded secret", fileStr, index + 1))
                            }
                        }
                    }
                }
        }

        return issues
    }
}
