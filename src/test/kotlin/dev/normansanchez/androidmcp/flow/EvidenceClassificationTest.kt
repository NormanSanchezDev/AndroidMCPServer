package dev.normansanchez.androidmcp.flow

import dev.normansanchez.androidmcp.fixtures.FixtureProjects
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvidenceClassificationTest {

    @Test
    fun `declared evidence keeps confidence 1_0 and inferred evidence exposes confidence`() {
        val temp = Files.createTempDirectory("evidence-class")
        try {
            val project = FixtureProjects.xmlFragmentApp(temp)
            val ir = FlowDetector.detect(project, DetectFlowOptions(maxDepth = 20))
            val flow = ir.flows.first { it.entryNode == "SplashActivity" }

            flow.edges.forEach { edge ->
                when (edge.evidenceType) {
                    EvidenceType.DECLARED -> assertEquals(1.0, edge.confidence)
                    EvidenceType.INFERRED -> assertTrue(edge.confidence < 1.0 && edge.confidence > 0.0)
                }
            }

            val hostEdge = flow.edges.first { it.from == "CheckoutActivity" && it.to == "CartFragment" }
            assertEquals(EvidenceType.INFERRED, hostEdge.evidenceType)
            assertEquals(0.9, hostEdge.confidence)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `merged edge stays declared when any source is declared`() {
        val temp = Files.createTempDirectory("evidence-merge")
        try {
            val project = FixtureProjects.xmlFragmentApp(temp)
            val ir = FlowDetector.detect(project, DetectFlowOptions(maxDepth = 20))
            val cartToSummary = ir.flows.first { it.entryNode == "SplashActivity" }
                .edges.first { it.from == "CartFragment" && it.to == "SummaryFragment" }

            assertEquals(EvidenceType.DECLARED, cartToSummary.evidenceType)
            assertEquals(1.0, cartToSummary.confidence)
            val evidenceTypes = cartToSummary.source.map { it.evidenceType }.toSet()
            assertTrue(EvidenceType.DECLARED in evidenceTypes)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `node and edge source evidence records file symbol and line`() {
        val temp = Files.createTempDirectory("evidence-source")
        try {
            val project = FixtureProjects.composeApp(temp)
            val ir = FlowDetector.detect(project, DetectFlowOptions(maxDepth = 10))
            val flow = ir.flows.first { it.entryNode == "MainActivity" }

            val splashToHome = flow.edges.firstOrNull { it.from == "SplashScreen" && it.to == "home" }
            assertTrue(splashToHome != null)
            val spot = splashToHome.source.first()
            assertTrue(spot.file.endsWith("SplashScreen.kt"))
            assertTrue(spot.line != null)
            assertEquals(EvidenceType.DECLARED, spot.evidenceType)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}