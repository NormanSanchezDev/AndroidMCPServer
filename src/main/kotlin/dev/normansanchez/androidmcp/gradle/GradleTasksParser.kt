package dev.normansanchez.androidmcp.gradle

data class GradleTaskEntry(
    val name: String,
    val group: String,
    val description: String?
)

object GradleTasksParser {

    private val taskLineRegex = Regex("^([a-zA-Z][a-zA-Z0-9]*)(?:\\s+-\\s(.*))?$")
    private val sectionSeparatorRegex = Regex("^-+$")

    fun parse(output: String): List<GradleTaskEntry> {
        val tasks = mutableListOf<GradleTaskEntry>()
        var currentGroup = ""

        val lines = output.lineSequence().toList()
        var index = 0

        while (index < lines.size) {
            val line = lines[index]
            val next = lines.getOrNull(index + 1)

            if (!line.isBlank() && next != null && sectionSeparatorRegex.matches(next.trim())) {
                if (sectionSeparatorRegex.matches(line.trim()) || line.startsWith(" ")) {
                    index += 1
                    continue
                }
                currentGroup = line.trim()
                index += 2
                continue
            }

            if (line.isBlank() || sectionSeparatorRegex.matches(line.trim()) || line.startsWith(">")) {
                index += 1
                continue
            }

            // Continuation of a wrapped multi-line description.
            if (line.startsWith(" ")) {
                val last = tasks.lastOrNull()
                if (last != null && last.description != null) {
                    tasks[tasks.lastIndex] =
                        last.copy(description = last.description + " " + line.trim())
                }
                index += 1
                continue
            }

            val match = taskLineRegex.matchEntire(line)
            if (match != null) {
                tasks.add(
                    GradleTaskEntry(
                        name = match.groupValues[1],
                        group = currentGroup,
                        description = match.groupValues[2].takeIf { it.isNotBlank() }
                    )
                )
            }

            index += 1
        }

        return tasks
    }
}
