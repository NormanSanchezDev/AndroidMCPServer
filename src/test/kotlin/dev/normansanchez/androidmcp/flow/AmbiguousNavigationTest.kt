package dev.normansanchez.androidmcp.flow

import dev.normansanchez.androidmcp.fixtures.FixtureProjects
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AmbiguousNavigationTest {

    @Test
    fun `multiple launchers are reported without silently picking one`() {
        val temp = Files.createTempDirectory("ambiguity-entry")
        try {
            val project = FixtureProjects.ambiguousEntryApp(temp)

            val ir = FlowDetector.detect(project, DetectFlowOptions(maxDepth = 10))

            assertEquals(3, ir.application.entryPoints.size)
            assertTrue(
                ir.ambiguities.any { it.kind == "entry_point" },
                "ambiguity must be reported instead of picking a launcher silently"
            )
            val entryAmbiguity = ir.ambiguities.first { it.kind == "entry_point" }
            assertTrue(entryAmbiguity.candidates.size >= 2)

            val seeded = ir.flows.map { it.entryNode }.toSet()
            assertTrue("SplashActivity" in seeded)
            assertTrue("MainActivity" in seeded)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `unresolved navigation target becomes external node and ambiguity`() {
        val temp = Files.createTempDirectory("ambiguity-unresolved")
        try {
            val project = FixtureProjects.xmlFragmentApp(temp)

            val ir = FlowDetector.detect(
                project,
                DetectFlowOptions(maxDepth = 20, knownEntryPoints = listOf("MainActivity"))
            )

            val flow = ir.flows.first { it.entryNode == "MainActivity" }
            val external = flow.nodes.firstOrNull { it.id == "external:OrderHistoryFragment" }
            assertTrue(external != null, "unresolved fragment transaction is surfaced as an external node")
            assertEquals(FlowNodeType.EXTERNAL, external.type)

            assertTrue(
                ir.ambiguities.any { it.kind == "navigation" && "OrderHistoryFragment" in it.candidates },
                "unresolved target reported as a navigation ambiguity"
            )
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}