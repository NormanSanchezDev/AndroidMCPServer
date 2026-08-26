package dev.normansanchez.androidmcp.model

data class AndroidModuleEvidence(
    val name: String,
    val path: String,
    val type: AndroidModuleType,
    val buildFile: String,
    val manifest: String?
)