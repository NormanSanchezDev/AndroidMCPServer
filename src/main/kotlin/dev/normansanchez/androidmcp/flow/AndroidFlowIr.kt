package dev.normansanchez.androidmcp.flow

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AndroidFlowIr(
    @SerialName("application") val application: FlowApplication,
    @SerialName("flows") val flows: List<AndroidFlow>,
    @SerialName("ambiguities") val ambiguities: List<FlowAmbiguity> = emptyList(),
    @SerialName("warnings") val warnings: List<String> = emptyList()
)

@Serializable
data class FlowApplication(
    @SerialName("package_name") val packageName: String? = null,
    @SerialName("entry_points") val entryPoints: List<FlowEntryPoint> = emptyList(),
    @SerialName("application_modules") val applicationModules: List<String> = emptyList(),
    @SerialName("modules") val modules: List<FlowModule> = emptyList(),
    @SerialName("module_dependencies") val moduleDependencies: List<ModuleDependency> = emptyList()
)

@Serializable
data class FlowEntryPoint(
    @SerialName("component") val component: String,
    @SerialName("kind") val kind: String,
    @SerialName("target_component") val targetComponent: String? = null,
    @SerialName("module") val module: String? = null,
    @SerialName("is_splash") val isSplash: Boolean = false,
    @SerialName("evidence") val evidence: SourceEvidence
)

@Serializable
data class FlowModule(
    @SerialName("name") val name: String,
    @SerialName("type") val type: String,
    @SerialName("path") val path: String
)

@Serializable
data class ModuleDependency(
    @SerialName("from") val from: String,
    @SerialName("to") val to: String,
    @SerialName("configuration") val configuration: String? = null
)

@Serializable
data class AndroidFlow(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("entry_node") val entryNode: String? = null,
    @SerialName("nodes") val nodes: List<FlowNode>,
    @SerialName("edges") val edges: List<FlowEdge>
)

@Serializable
enum class FlowNodeType {
    @SerialName("SCREEN")
    SCREEN,
    @SerialName("ACTION")
    ACTION,
    @SerialName("DECISION")
    DECISION,
    @SerialName("PROCESS")
    PROCESS,
    @SerialName("EXTERNAL")
    EXTERNAL,
    @SerialName("UNKNOWN")
    UNKNOWN
}

@Serializable
enum class UiFramework {
    @SerialName("COMPOSE")
    COMPOSE,
    @SerialName("XML")
    XML,
    @SerialName("MIXED")
    MIXED,
    @SerialName("UNKNOWN")
    UNKNOWN
}

@Serializable
enum class EvidenceType {
    @SerialName("DECLARED")
    DECLARED,
    @SerialName("INFERRED")
    INFERRED
}

@Serializable
enum class SelectorKind {
    @SerialName("RESOURCE_ID")
    RESOURCE_ID,
    @SerialName("TEST_TAG")
    TEST_TAG,
    @SerialName("CONTENT_DESCRIPTION")
    CONTENT_DESCRIPTION,
    @SerialName("TEXT")
    TEXT
}

@Serializable
enum class SelectorStatus {
    @SerialName("RESOLVED")
    RESOLVED,
    @SerialName("MISSING")
    MISSING
}

@Serializable
data class SourceEvidence(
    @SerialName("file") val file: String,
    @SerialName("symbol") val symbol: String? = null,
    @SerialName("line") val line: Int? = null,
    @SerialName("evidence_type") val evidenceType: EvidenceType,
    @SerialName("confidence") val confidence: Double
)

@Serializable
data class SourceLocation(
    @SerialName("file") val file: String,
    @SerialName("symbol") val symbol: String? = null,
    @SerialName("line") val line: Int? = null
)

@Serializable
data class Selector(
    @SerialName("kind") val kind: SelectorKind,
    @SerialName("value") val value: String
)

@Serializable
data class FlowNode(
    @SerialName("id") val id: String,
    @SerialName("type") val type: FlowNodeType,
    @SerialName("name") val name: String? = null,
    @SerialName("condition") val condition: String? = null,
    @SerialName("ui_framework") val uiFramework: UiFramework = UiFramework.UNKNOWN,
    @SerialName("source") val source: SourceLocation? = null,
    @SerialName("route") val route: String? = null,
    @SerialName("layout") val layout: String? = null,
    @SerialName("class_name") val className: String? = null,
    @SerialName("selectors") val selectors: List<Selector> = emptyList(),
    @SerialName("selector_status") val selectorStatus: SelectorStatus? = null,
    @SerialName("start_destination") val startDestination: Boolean = false,
    @SerialName("deep_links") val deepLinks: List<String> = emptyList(),
    @SerialName("evidence") val evidence: List<SourceEvidence> = emptyList()
)

@Serializable
data class FlowEdge(
    @SerialName("from") val from: String,
    @SerialName("to") val to: String,
    @SerialName("condition") val condition: String? = null,
    @SerialName("label") val label: String? = null,
    @SerialName("evidence_type") val evidenceType: EvidenceType,
    @SerialName("confidence") val confidence: Double,
    @SerialName("source") val source: List<SourceEvidence> = emptyList()
)

@Serializable
data class FlowAmbiguity(
    @SerialName("kind") val kind: String,
    @SerialName("description") val description: String,
    @SerialName("candidates") val candidates: List<String> = emptyList()
)