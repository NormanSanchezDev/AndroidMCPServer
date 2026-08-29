package dev.normansanchez.androidmcp.util

import java.nio.file.Path

val DEFAULT_EXCLUDED_DIR_NAMES = setOf(
    "build", ".gradle", ".git", ".idea", "node_modules", ".kotlin"
)

fun Path.resolveModuleOrNull(module: String?): Path? {
    val clean = module?.replace('\\', '/')?.trim()
    if (clean.isNullOrBlank()) return this

    val root = toAbsolutePath().normalize()
    val resolved = root.resolve(clean.removePrefix(":")).toAbsolutePath().normalize()
    return resolved.takeIf { it.startsWith(root) }
}

fun Path.isUnderExcludedDir(root: Path, extra: Set<String> = emptySet()): Boolean {
    val names = DEFAULT_EXCLUDED_DIR_NAMES + extra
    val relative = root.relativize(this).toString().replace("\\", "/")
    return relative.split("/").any { it in names }
}