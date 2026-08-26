package dev.normansanchez.androidmcp.fixtures

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString

object GradleFixtureProject {

    fun create(parentDir: Path): Path {
        val project = parentDir.resolve("minimal-gradle")
        Files.createDirectories(project)

        Files.writeString(
            project.resolve("settings.gradle.kts"),
            """
            rootProject.name = "minimal-gradle"
            """.trimIndent() + "\n"
        )

        val repoRoot = Path.of(System.getProperty("user.dir")).toAbsolutePath()
        copyFile(repoRoot.resolve("gradlew"), project.resolve("gradlew"))
        makeExecutable(project.resolve("gradlew"))

        copyFile(repoRoot.resolve("gradlew.bat"), project.resolve("gradlew.bat"))

        val sourceWrapperDir = repoRoot.resolve("gradle/wrapper")
        val targetWrapperDir = project.resolve("gradle/wrapper")
        Files.createDirectories(targetWrapperDir)
        Files.list(sourceWrapperDir).use { entries ->
            entries.filter { Files.isRegularFile(it) }.forEach { file ->
                copyFile(file, targetWrapperDir.resolve(file.fileName.toString()))
            }
        }

        return project
    }

    private fun copyFile(source: Path, target: Path) {
        if (!Files.isRegularFile(source)) return
        Files.copy(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
    }

    private fun makeExecutable(path: Path) {
        val view = Files.getFileAttributeView(path, java.nio.file.attribute.PosixFileAttributeView::class.java)
            ?: return
        val permissions = view.readAttributes().permissions()
        permissions.add(java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE)
        view.setPermissions(permissions)
    }
}
