package latticeMCP.fixtures

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString

object FixtureProjects {

    fun sampleAndroidProject(tempDir: Path): Path {
        val target = tempDir.resolve("sample-android-project")
        val source = resourceDirectory("fixtures/sample-android-project")
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
