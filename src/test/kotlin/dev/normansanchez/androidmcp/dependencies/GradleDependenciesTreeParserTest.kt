package dev.normansanchez.androidmcp.dependencies

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GradleDependenciesTreeParserTest {

    @Test
    fun `parses real gradle dependencies output with conflicts and markers`() {
        val output = resource("fixtures/gradle-deps/dependencies-root.txt")

        val configurations = GradleDependenciesTreeParser.parse(output)

        assertTrue(configurations.size >= 2)

        val runtime = configurations.first { it.configuration == "runtimeClasspath" }

        val conflictedAnnotations =
            runtime.dependencies.filter { it.name == "annotations" && it.omittedByConflict }
        assertTrue(conflictedAnnotations.isNotEmpty())
        assertEquals("13.0", conflictedAnnotations.first().requestedVersion)
        assertEquals("23.0.0", conflictedAnnotations.first().version)
        assertEquals("org.jetbrains", conflictedAnnotations.first().group)

        val stdlibConflict = runtime.dependencies.first {
            it.group == "org.jetbrains.kotlin" &&
                    it.name == "kotlin-stdlib" &&
                    it.omittedByConflict
        }
        assertEquals("2.2.20", stdlibConflict.requestedVersion)
        assertEquals("2.4.0", stdlibConflict.version)
        assertTrue(stdlibConflict.repeatedSubtreeOmitted)

        assertTrue(
            runtime.dependencies.none { it.name.contains("(c)") },
            "(c) constraint lines must not become dependency nodes"
        )
    }

    @Test
    fun `parses project dependencies and multiple configurations`() {
        val output = listOf(
            "",
            "> Task :app:dependencies",
            "",
            "debugRuntimeClasspath - Runtime classpath for 'debug'.",
            "+--- project :core-data",
            "|    +--- androidx.core:core-ktx:1.15.0",
            "|    |    \\--- androidx.annotation:annotation-jvm:1.8.1",
            "|    \\--- org.jetbrains.kotlin:kotlin-stdlib:2.0.21 -> 2.4.0 (*)",
            "\\--- com.squareup.okhttp3:okhttp:4.12.0",
            "     \\--- com.squareup.okio:okio-jvm:3.6.0",
            "",
            "releaseCompileClasspath - Compile classpath for 'release'.",
            "+--- project :core-data (*)",
            "\\--- io.mockk:mockk-agent-jvm:1.13.13",
            ""
        ).joinToString("\n")

        val configurations = GradleDependenciesTreeParser.parse(output)

        assertEquals(2, configurations.size)

        val debugRuntime = configurations[0]
        assertEquals("debugRuntimeClasspath", debugRuntime.configuration)
        assertEquals(5, debugRuntime.dependencies.size)

        val coreData = debugRuntime.dependencies.first()
        assertTrue(coreData.isProjectDependency)
        assertEquals(":core-data", coreData.name)

        val conflict = debugRuntime.dependencies.first { it.omittedByConflict }
        assertEquals("kotlin-stdlib", conflict.name)
        assertEquals("2.0.21", conflict.requestedVersion)
        assertEquals("2.4.0", conflict.version)

        val releaseCompile = configurations[1]
        assertEquals(2, releaseCompile.dependencies.size)
        assertTrue(releaseCompile.dependencies.first().isProjectDependency)
        assertTrue(releaseCompile.dependencies.first().repeatedSubtreeOmitted)
    }

    private fun resource(path: String): String =
        GradleDependenciesTreeParserTest::class.java.classLoader.getResource(path)!!.readText()
}
