package latticeMCP.process

import java.io.File
import java.util.concurrent.TimeUnit

data class ProcessResult(
    val command: List<String>,
    val workingDirectory: File,
    val exitCode: Int?,
    val stdout: String,
    val stderr: String,
    val timedOut: Boolean,
    val durationMs: Long
) {
    val success: Boolean
        get() = !timedOut && exitCode == 0
}

object ProcessExecutor {

    fun execute(
        command: List<String>,
        workingDirectory: File,
        timeoutSeconds: Long = 600,
        maxCapturedChars: Int = DEFAULT_MAX_CAPTURED_CHARS
    ): ProcessResult {
        require(command.isNotEmpty()) { "command must not be empty" }
        require(workingDirectory.isDirectory) { "working directory does not exist: $workingDirectory" }

        val startedAt = System.currentTimeMillis()

        val process = ProcessBuilder(command)
            .directory(workingDirectory)
            .redirectErrorStream(false)
            .start()

        // Read both streams concurrently to avoid pipe-buffer deadlocks.
        var stdoutText = ""
        var stderrText = ""

        val stdoutThread = Thread {
            process.inputStream.bufferedReader().use { stdoutText = it.readText() }
        }
        val stderrThread = Thread {
            process.errorStream.bufferedReader().use { stderrText = it.readText() }
        }

        stdoutThread.isDaemon = true
        stderrThread.isDaemon = true
        stdoutThread.start()
        stderrThread.start()

        val finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)

        if (!finished) {
            process.destroyForcibly()
            process.waitFor(10, TimeUnit.SECONDS)
        }

        stdoutThread.join(10_000)
        stderrThread.join(10_000)

        val durationMs = System.currentTimeMillis() - startedAt

        return ProcessResult(
            command = command,
            workingDirectory = workingDirectory,
            exitCode = if (finished) process.exitValue() else null,
            stdout = truncate(stdoutText, maxCapturedChars),
            stderr = truncate(stderrText, maxCapturedChars),
            timedOut = !finished,
            durationMs = durationMs
        )
    }

    private fun truncate(text: String, maxChars: Int): String =
        if (text.length <= maxChars) text else text.substring(0, maxChars)

    const val DEFAULT_MAX_CAPTURED_CHARS: Int = 400_000
}
