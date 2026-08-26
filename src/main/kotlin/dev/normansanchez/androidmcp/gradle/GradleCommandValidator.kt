package dev.normansanchez.androidmcp.gradle

import java.nio.file.Path

object GradleCommandValidator {

    private val taskNameRegex = Regex("^[a-zA-Z][a-zA-Z0-9]*(?::[a-zA-Z][a-zA-Z0-9]*)*$")

    val allowedFlags: Set<String> = setOf(
        "--parallel",
        "--build-cache",
        "--configuration-cache",
        "--continue",
        "--no-daemon",
        "--info",
        "--quiet",
        "--console=plain"
    )

    fun validateTaskName(name: String): Boolean = taskNameRegex.matches(name)

    fun validateFlag(flag: String): Boolean = flag in allowedFlags

    fun buildCommand(
        wrapper: Path,
        tasks: List<String>,
        flags: List<String> = emptyList()
    ): List<String> {
        val invalidTask = tasks.firstOrNull { !validateTaskName(it) }
        require(invalidTask == null) { "Invalid task name: $invalidTask" }

        val invalidFlag = flags.firstOrNull { !validateFlag(it) }
        require(invalidFlag == null) { "Flag not allowed: $invalidFlag" }

        return listOf(wrapper.toString()) + tasks + flags
    }
}
