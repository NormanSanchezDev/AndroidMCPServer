package dev.normansanchez.androidmcp.flow

import dev.normansanchez.androidmcp.fixtures.FixtureProjects
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class XmlNavigationTest {

    @Test
    fun `parses start destination actions and deep links from nav xml`() {
        val temp = Files.createTempDirectory("xmlnav-parse")
        try {
            val project = FixtureProjects.xmlFragmentApp(temp)
            val navFile = project.resolve("app/src/main/res/navigation/nav_cart.xml")

            val graph = XmlNavGraphParser.parseFile(navFile, project)!!

            assertEquals("cartFragment", graph.startDestination)
            assertEquals("nav_cart", graph.graphId)
            assertEquals(
                setOf("cartFragment", "summaryFragment", "orderDoneFragment"),
                graph.destinations.map { it.resourceId }.toSet()
            )
            assertEquals(
                "com.acme.shop.cart.CartFragment",
                graph.destinations.first { it.resourceId == "cartFragment" }.className
            )
            assertTrue(graph.destinations.first { it.resourceId == "cartFragment" }.isStartDestination)
            assertEquals(
                listOf("acme://shop/orders/done"),
                graph.destinations.first { it.resourceId == "orderDoneFragment" }.deepLinks
            )
            val actions = graph.actions.map { it.actionId to (it.from to it.to) }.toSet()
            assertTrue("action_cartToSummary" to ("cartFragment" to "summaryFragment") in actions)
            assertTrue("action_summaryToDone" to ("summaryFragment" to "orderDoneFragment") in actions)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `flow graph exposes xml destinations actions and host linkage`() {
        val temp = Files.createTempDirectory("xmlnav-irtest")
        try {
            val project = FixtureProjects.xmlFragmentApp(temp)

            val ir = FlowDetector.detect(
                project,
                DetectFlowOptions(entryPoint = "auto", maxDepth = 20)
            )

            val flow = ir.flows.first { it.entryNode == "SplashActivity" }
            val nodeIds = flow.nodes.map { it.id }.toSet()

            assertTrue("CartFragment" in nodeIds)
            assertTrue("SummaryFragment" in nodeIds)
            assertTrue("OrderDoneFragment" in nodeIds)

            val edges = flow.edges
            assertTrue(edges.any { it.from == "CartFragment" && it.to == "SummaryFragment" })
            assertTrue(edges.any { it.from == "SummaryFragment" && it.to == "OrderDoneFragment" })
            assertTrue(edges.any { it.from == "CheckoutActivity" && it.to == "CartFragment" })

            val orderDone = flow.nodes.first { it.id == "OrderDoneFragment" }
            assertTrue(orderDone.startDestination || flow.nodes.any { it.startDestination })
            assertEquals(UiFramework.XML, orderDone.uiFramework)

            val actionEdge = edges.first { it.from == "CartFragment" && it.to == "SummaryFragment" }
            assertEquals(EvidenceType.DECLARED, actionEdge.evidenceType, "xml action + navigate call are declared")
            assertEquals(1.0, actionEdge.confidence)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}