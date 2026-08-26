package dev.normansanchez.androidmcp.gradle

import java.nio.file.Files
import java.nio.file.Path

object GradleWrapperLocator {

    fun findWrapper(projectRoot: Path): Path? {
        val unixWrapper = projectRoot.resolve("gradlew")
        if (Files.isRegularFile(unixWrapper) && Files.isExecutable(unixWrapper)) {
            return unixWrapper
        }

        val windowsWrapper = projectRoot.resolve("gradlew.bat")
        if (Files.isRegularFile(windowsWrapper)) {
            return windowsWrapper
        }

        return null
    }
}
