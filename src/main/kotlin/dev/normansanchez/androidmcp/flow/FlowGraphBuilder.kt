package dev.normansanchez.androidmcp.flow

private class MutableEdge(
    val from: String,
    val to: String,
    var label: String?,
    var condition: String?,
    val evidences: MutableList<SourceEvidence>
)

class FlowGraphBuilder {

    private val nodes = LinkedHashMap<String, FlowNode>()
    private val edges = LinkedHashMap<String, MutableEdge>()

    private val routeToNode = HashMap<String, String>()
    private val classNameToNode = HashMap<String, String>()
    private val resourceIdToNode = HashMap<String, String>()
    private val actionIdToTarget = HashMap<String, String>()

    fun registerRoute(route: String, nodeId: String) {
        routeToNode[route] = nodeId
    }

    fun registerClassName(className: String, nodeId: String) {
        classNameToNode[SourceScanner.simpleName(className)] = nodeId
    }

    fun registerResourceId(resourceId: String, nodeId: String) {
        resourceIdToNode[resourceId] = nodeId
    }

    fun registerActionId(actionId: String, targetNodeId: String) {
        if (actionId.isNotBlank()) {
            actionIdToTarget[actionId] = targetNodeId
        }
    }

    fun resolve(token: String): String? {
        val trimmed = token.trim()
        return when {
            trimmed.startsWith("R.id.") -> {
                val name = trimmed.removePrefix("R.id.")
                actionIdToTarget[name] ?: resourceIdToNode[name]
            }

            trimmed.startsWith("R.navigation.") -> null
            trimmed.startsWith("@id/") -> resourceIdToNode[trimmed.removePrefix("@id/")]
            trimmed.startsWith("\"") || trimmed.startsWith("'") -> {
                routeToNode[trimmed.trim('"').trim('\'')]
            }

            else -> classNameToNode[SourceScanner.simpleName(trimmed)] ?: routeToNode[trimmed]
        }
    }

    fun addNode(node: FlowNode) {
        val existing = nodes[node.id]
        if (existing == null) {
            nodes[node.id] = node
            return
        }
        nodes[node.id] = existing.copy(
            type = if (existing.type == FlowNodeType.UNKNOWN) node.type else existing.type,
            name = existing.name ?: node.name,
            condition = existing.condition ?: node.condition,
            uiFramework = if (existing.uiFramework == UiFramework.UNKNOWN) node.uiFramework else {
                if (existing.uiFramework == node.uiFramework || node.uiFramework == UiFramework.UNKNOWN) {
                    existing.uiFramework
                } else {
                    UiFramework.MIXED
                }
            },
            source = existing.source ?: node.source,
            route = existing.route ?: node.route,
            layout = existing.layout ?: node.layout,
            className = existing.className ?: node.className,
            selectors = if (existing.selectors.isNotEmpty()) existing.selectors else node.selectors,
            selectorStatus = existing.selectorStatus ?: node.selectorStatus,
            startDestination = existing.startDestination || node.startDestination,
            deepLinks = (existing.deepLinks + node.deepLinks).distinct(),
            evidence = (existing.evidence + node.evidence).distinct()
        )
    }

    fun node(id: String): FlowNode? = nodes[id]

    fun addEdge(
        from: String,
        to: String,
        condition: String? = null,
        label: String? = null,
        evidence: SourceEvidence
    ) {
        if (from.isBlank() || to.isBlank() || from == to) return
        val key = "$from\u0000$to"
        val edge = edges.getOrPut(key) { MutableEdge(from, to, label, condition, mutableListOf()) }
        if (edge.evidences.none { it == evidence }) {
            edge.evidences.add(evidence)
        }
        if (edge.label == null && label != null) edge.label = label
        if (edge.condition == null && condition != null) edge.condition = condition
    }

    fun buildFlows(seeds: List<String>, maxDepth: Int): List<AndroidFlow> {
        val flows = mutableListOf<AndroidFlow>()
        val seenSeeds = HashSet<String>()
        seeds.forEach { seed ->
            if (!seenSeeds.add(seed)) return@forEach
            if (!nodes.containsKey(seed) && seed != "") return@forEach
            val visited = LinkedHashSet<String>()
            val queue = ArrayDeque<Pair<String, Int>>()
            queue.add(seed to 0)
            visited.add(seed)
            while (queue.isNotEmpty()) {
                val (current, depth) = queue.removeFirst()
                if (depth >= maxDepth) continue
                val outgoing = edges.values.filter { it.from == current }
                for (edge in outgoing) {
                    if (visited.add(edge.to)) {
                        queue.add(edge.to to depth + 1)
                    }
                }
            }
            val flowNodes = visited.mapNotNull { nodes[it] }
            val flowEdges = edges.values
                .filter { it.from in visited && it.to in visited }
                .mapNotNull { it.toFlowEdge() }
            flows.add(
                AndroidFlow(
                    id = "flow.$seed",
                    name = seed,
                    entryNode = seed,
                    nodes = flowNodes,
                    edges = flowEdges
                )
            )
        }
        return flows
    }

    fun nodesSnapshot(): List<FlowNode> = nodes.values.toList()

    fun edgesSnapshot(): List<FlowEdge> = edges.values.mapNotNull { it.toFlowEdge() }

    private fun MutableEdge.toFlowEdge(): FlowEdge? {
        if (evidences.isEmpty()) return null
        val declared = evidences.any { it.evidenceType == EvidenceType.DECLARED }
        val confidence = evidences.maxOf { it.confidence }
        return FlowEdge(
            from = from,
            to = to,
            condition = condition,
            label = label,
            evidenceType = if (declared) EvidenceType.DECLARED else EvidenceType.INFERRED,
            confidence = confidence,
            source = evidences.distinct()
        )
    }
}