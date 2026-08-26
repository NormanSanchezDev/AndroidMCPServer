package dev.normansanchez.androidmcp.model

enum class AndroidModuleType(
    val value: String,
    val pluginId: String
) {
    APPLICATION(
        value = "application",
        pluginId = "com.android.application"
    ),
    LIBRARY(
        value = "library",
        pluginId = "com.android.library"
    ),
    UNKNOWN(
        value = "unknown",
        pluginId = ""
    )
}