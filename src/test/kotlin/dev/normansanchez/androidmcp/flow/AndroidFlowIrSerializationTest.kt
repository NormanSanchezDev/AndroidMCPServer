package dev.normansanchez.androidmcp.flow

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AndroidFlowIrSerializationTest {

    private val json = Json { prettyPrint = false; encodeDefaults = true }

    @Test
    fun `ir round-trips through kotlinx serialization unchanged`() {
        val source = buildIr()
        val text = json.encodeToString(AndroidFlowIr.serializer(), source)
        val decoded = json.decodeFromString(AndroidFlowIr.serializer(), text)

        assertEquals(source, decoded)
    }

    @Test
    fun `serialized ir uses the documented snake_case contract`() {
        val element = Json.parseToJsonElement(json.encodeToString(AndroidFlowIr.serializer(), buildIr()))
            .jsonObject

        val application = element["application"]!!.jsonObject
        assertTrue("package_name" in application)
        assertTrue("entry_points" in application)
        assertTrue("application_modules" in application)

        val entry = application["entry_points"]!!.jsonArray.first().jsonObject
        assertTrue("target_component" in entry, "target_component key present when set")
        assertEquals("com.acme.shop.ui.SplashActivity", entry["target_component"]!!.jsonPrimitive.content)
        assertTrue("isolated_key" !in entry)

        val firstFlow = element["flows"]!!.jsonArray.first().jsonObject
        assertTrue("entry_node" in firstFlow)

        val node = firstFlow["nodes"]!!.jsonArray.first().jsonObject
        assertTrue("ui_framework" in node)
        assertTrue("selector_status" in node)
        assertTrue("evidence_type" in node["evidence"]!!.jsonArray.first().jsonObject)
        val firstNodeType = node["type"]!!.jsonPrimitive.content
        assertTrue(firstNodeType == "SCREEN" || firstNodeType == "DECISION" || firstNodeType == "ACTION")
    }

    private fun buildIr(): AndroidFlowIr {
        val evidence = SourceEvidence(
            file = "app/src/main/res/navigation/nav_cart.xml",
            symbol = "cartFragment",
            evidenceType = EvidenceType.DECLARED,
            confidence = 1.0
        )
        return AndroidFlowIr(
            application = FlowApplication(
                packageName = "com.acme.shop",
                entryPoints = listOf(
                    FlowEntryPoint(
                        component = "com.acme.shop.SplashActivity",
                        kind = "activity",
                        targetComponent = "com.acme.shop.ui.SplashActivity",
                        module = "app",
                        evidence = SourceEvidence(
                            file = "app/src/main/AndroidManifest.xml",
                            symbol = "com.acme.shop.ui.SplashActivity",
                            evidenceType = EvidenceType.DECLARED,
                            confidence = 1.0
                        )
                    )
                ),
                applicationModules = listOf("app"),
                modules = listOf(FlowModule("app", "APPLICATION", "app")),
                moduleDependencies = emptyList()
            ),
            flows = listOf(
                AndroidFlow(
                    id = "flow.test",
                    name = "test",
                    entryNode = "SplashActivity",
                    nodes = listOf(
                        FlowNode(
                            id = "CartFragment",
                            type = FlowNodeType.SCREEN,
                            name = "CartFragment",
                            uiFramework = UiFramework.XML,
                            selectorStatus = null,
                            evidence = listOf(evidence)
                        )
                    ),
                    edges = listOf(
                        FlowEdge(
                            from = "CartFragment",
                            to = "SummaryFragment",
                            label = "action action_cartToSummary",
                            evidenceType = EvidenceType.DECLARED,
                            confidence = 1.0,
                            source = listOf(evidence)
                        )
                    )
                )
            ),
            ambiguities = listOf(
                FlowAmbiguity(
                    kind = "navigation",
                    description = "unresolved",
                    candidates = listOf("X")
                )
            ),
            warnings = listOf("sample")
        )
    }
}