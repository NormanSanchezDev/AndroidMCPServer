package dev.normansanchez.androidmcp.symbol

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.relativeTo

data class ScannedKotlinFile(
    val path: Path,
    val relativePath: String,
    val content: String,
    val ktFile: org.jetbrains.kotlin.psi.KtFile
)

object KotlinSourceScanner {

    private const val MAX_DISCOVERY_DEPTH = 6

    private val EXCLUDED_DIR_NAMES = setOf(
        "build", ".gradle", ".git", ".idea", "node_modules", ".kotlin"
    )

    fun scan(
        projectRoot: Path,
        includeTests: Boolean,
        maxFiles: Int = 2_000
    ): List<ScannedKotlinFile> {
        val sourceRoots = discoverSourceRoots(projectRoot, includeTests)

        val scanned = mutableListOf<ScannedKotlinFile>()

        for (root in sourceRoots) {
            if (scanned.size >= maxFiles) break

            Files.walk(root).use { paths ->
                paths.filter { Files.isRegularFile(it) && it.extension == "kt" }
                    .sorted()
                    .forEach { file ->
                        if (scanned.size >= maxFiles) return@forEach

                        val content = try {
                            Files.readString(file)
                        } catch (_: Exception) {
                            null
                        } ?: return@forEach

                        val ktFile = KotlinPsiEngine.parse(content, file.fileName.toString())
                        scanned.add(
                            ScannedKotlinFile(
                                path = file,
                                relativePath = file.relativeTo(projectRoot).toString(),
                                content = content,
                                ktFile = ktFile
                            )
                        )
                    }
            }
        }

        return scanned
    }

    private fun discoverSourceRoots(
        projectRoot: Path,
        includeTests: Boolean
    ): List<Path> {
        val roots = mutableListOf<Path>()

        Files.walk(projectRoot, MAX_DISCOVERY_DEPTH).use { paths ->
            paths.filter { Files.isDirectory(it) && !isExcluded(it) }
                .forEach { dir ->
                    val parentName = dir.parent?.name ?: return@forEach
                    val grandParentName = dir.parent?.parent?.name ?: return@forEach

                    val sourceSetName = when {
                        grandParentName == "src" -> parentName
                        else -> return@forEach
                    }

                    val isMain = sourceSetName == "main"
                    val isTest = sourceSetName.startsWith("test") || sourceSetName.startsWith("androidTest")

                    val languageDir = dir.name
                    if ((languageDir == "java" || languageDir == "kotlin") &&
                        (isMain || (includeTests && isTest))
                    ) {
                        roots.add(dir)
                    }
                }
        }

        return roots.sorted()
    }

    private fun isExcluded(dir: Path): Boolean =
        dir.fileName.toString() in EXCLUDED_DIR_NAMES ||
                dir.name.endsWith(".d")
}
