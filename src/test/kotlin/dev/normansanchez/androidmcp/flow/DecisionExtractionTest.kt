package dev.normansanchez.androidmcp.flow

import dev.normansanchez.androidmcp.fixtures.FixtureProjects
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DecisionExtractionTest {

    @Test
    fun `if-else with startActivity produces true and false outcomes`() {
        val temp = Files.createTempDirectory("decision-if")
        try {
            val project = FixtureProjects.xmlFragmentApp(temp)
            val splash = Files.readString(
                project.resolve("app/src/main/java/com/acme/shop/ui/SplashActivity.kt")
            )

            val decisions = DecisionExtractor.decisions(splash)

            val decision = decisions.single { it.condition.contains("token == null") }
            assertEquals(2, decision.outcomes.size)
            val outcomes = decision.outcomes.associate { it.label to it.target }
            assertEquals("LoginActivity", outcomes["true"])
            assertEquals("MainActivity", outcomes["false"])
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `when over sealed state routes success branch to action`() {
        val temp = Files.createTempDirectory("decision-when")
        try {
            val project = FixtureProjects.xmlFragmentApp(temp)
            val cart = Files.readString(
                project.resolve("app/src/main/java/com/acme/shop/cart/CartFragment.kt")
            )

            val decisions = DecisionExtractor.decisions(cart)

            val decision = decisions.single { it.condition == "when (state)" }
            val success = decision.outcomes.first { it.label == "is CheckoutUi.Success" }
            assertEquals("R.id.action_cartToSummary", success.target)

            val ir = FlowDetector.detect(project, DetectFlowOptions(maxDepth = 20))
            val decisionNodes = ir.flows.first { it.entryNode == "SplashActivity" }
                .nodes.filter { it.type == FlowNodeType.DECISION }
            assertTrue(decisionNodes.isNotEmpty(), "decision nodes are part of the flow")
            assertTrue(decisionNodes.any { it.condition == "when (state)" })
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `compose when over session state routes to routes`() {
        val temp = Files.createTempDirectory("decision-compose")
        try {
            val project = FixtureProjects.composeApp(temp)
            val splash = Files.readString(
                project.resolve("app/src/main/java/com/acme/auth/ui/SplashScreen.kt")
            )

            val decisions = DecisionExtractor.decisions(splash)

            val decision = decisions.single { it.condition == "when (session)" }
            val outcomes = decision.outcomes.associate { it.label to it.target }
            assertEquals("home", outcomes["is SessionState.Authenticated"])
            assertEquals("login", outcomes["is SessionState.Expired"])
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `decision edges are inferred with sub-unity confidence`() {
        val temp = Files.createTempDirectory("decision-evidence")
        try {
            val project = FixtureProjects.xmlFragmentApp(temp)
            val ir = FlowDetector.detect(project, DetectFlowOptions(maxDepth = 20))
            val splashFlow = ir.flows.first { it.entryNode == "SplashActivity" }
            val decisionOutEdges = splashFlow.edges.filter { it.from.startsWith("SplashActivity.decision_") }
            assertTrue(decisionOutEdges.isNotEmpty())
            decisionOutEdges.forEach { edge ->
                assertEquals(EvidenceType.INFERRED, edge.evidenceType)
                assertTrue(edge.confidence < 1.0)
            }
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}