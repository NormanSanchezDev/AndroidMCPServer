package dev.normansanchez.androidmcp.fixtures

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString

object FixtureProjects {

    fun sampleAndroidProject(tempDir: Path): Path = copyFixture(tempDir, "sample-android-project")

    fun xmlFragmentApp(tempDir: Path): Path = copyFixture(tempDir, "xml-fragment-app")

    fun composeApp(tempDir: Path): Path = copyFixture(tempDir, "compose-app")

    fun ambiguousEntryApp(tempDir: Path): Path = copyFixture(tempDir, "ambiguous-entry-app")

    private fun copyFixture(tempDir: Path, fixture: String): Path {
        val target = tempDir.resolve(fixture)
        val source = resourceDirectory("fixtures/$fixture")
        source.toAbsolutePath().let { Files.walk(it).use { paths ->
            paths.forEach { path ->
                val relative = it.relativize(path)
                val destination = target.resolve(relative)
                if (Files.isDirectory(path)) {
                    Files.createDirectories(destination)
                } else {
                    Files.createDirectories(destination.parent)
                    Files.copy(path, destination)
                }
            }
        } }
        return target
    }

    private fun resourceDirectory(resourcePath: String): Path {
        val url = FixtureProjects::class.java.classLoader.getResource(resourcePath)
            ?: error("Fixture resource not found: $resourcePath")
        return Path.of(url.toURI())
    }
}
