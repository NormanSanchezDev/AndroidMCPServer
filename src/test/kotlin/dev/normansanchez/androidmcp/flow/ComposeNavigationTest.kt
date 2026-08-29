package dev.normansanchez.androidmcp.flow

import dev.normansanchez.androidmcp.fixtures.FixtureProjects
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ComposeNavigationTest {

    @Test
    fun `detects named-argument and multiline composable routes`() {
        val temp = Files.createTempDirectory("compose-routes")
        try {
            val project = FixtureProjects.composeApp(temp)
            val navFile = project.resolve("app/src/main/java/com/acme/auth/navigation/AppNavHost.kt")

            val routeInfo = ComposeNavParser.startDestinations(Files.readString(navFile))
            assertEquals(listOf("splash"), routeInfo)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `flow graph exposes compose routes and navigate edges`() {
        val temp = Files.createTempDirectory("compose-ir")
        try {
            val project = FixtureProjects.composeApp(temp)

            val ir = FlowDetector.detect(project, DetectFlowOptions(maxDepth = 10))

            assertEquals(1, ir.application.entryPoints.size)
            val flow = ir.flows.first { it.entryNode == "MainActivity" }
            val nodeIds = flow.nodes.map { it.id }.toSet()

            assertTrue("splash" in nodeIds, "startDestination route present")
            assertTrue("login" in nodeIds, "login route present")
            assertTrue("home" in nodeIds, "home route present")
            assertTrue("SplashScreen" in nodeIds)

            val splashNode = flow.nodes.first { it.id == "splash" }
            assertEquals("splash", splashNode.route)
            assertEquals(UiFramework.COMPOSE, splashNode.uiFramework)

            val edges = flow.edges
            assertTrue(edges.any { it.from == "AppNavRoot" && it.to == "splash" }, "NavHost start destination edge")
            assertTrue(edges.any { it.from == "SplashScreen" && it.to == "home" }, "navigate home")
            assertTrue(edges.any { it.from == "SplashScreen" && it.to == "login" }, "navigate login")
            assertTrue(edges.any { it.from == "MainActivity" && it.to == "AppNavRoot" }, "setContent edge")
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}