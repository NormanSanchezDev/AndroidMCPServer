package dev.normansanchez.androidmcp.gradle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GradleTasksParserTest {

    private fun realOutput(): String =
        GradleTasksParserTest::class.java.classLoader
            .getResource("fixtures/gradle/tasks-all-root.txt")!!
            .readText()

    @Test
    fun `parses tasks with groups and descriptions from real gradle output`() {
        val tasks = GradleTasksParser.parse(realOutput())

        assertTrue(tasks.size >= 20)

        val byName = tasks.associateBy { it.name }

        assertEquals("Application tasks", byName["run"]?.group)
        assertEquals("Runs this project as a JVM application", byName["run"]?.description)

        assertEquals("Build Setup tasks", byName["wrapper"]?.group)
        assertEquals("Generates Gradle wrapper files.", byName["wrapper"]?.description)

        assertEquals("Verification tasks", byName["check"]?.group)
        assertEquals("Runs all checks.", byName["check"]?.description)
    }

    @Test
    fun `does not treat banner lines as tasks`() {
        val tasks = GradleTasksParser.parse(realOutput())

        val names = tasks.map { it.name }
        assertTrue("Tasks" !in names)
        assertTrue("BUILD" !in names)
        assertTrue("Task" !in names)
    }

    @Test
    fun `handles task without description`() {
        val output = """
            Build tasks
            -----------
            assemble - Assembles the outputs of this project.
            plainTask

            BUILD SUCCESSFUL
        """.trimIndent()

        val tasks = GradleTasksParser.parse(output)

        assertEquals(2, tasks.size)
        assertNull(tasks[1].description)
        assertEquals("Build tasks", tasks[1].group)
    }
}
