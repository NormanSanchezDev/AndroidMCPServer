package dev.normansanchez.androidmcp.flow

object MermaidRenderer {

    fun renderAll(flows: List<AndroidFlow>): String =
        flows.joinToString("\n\n") { render(it) }

    fun render(flow: AndroidFlow): String {
        val builder = StringBuilder()
        builder.appendLine("flowchart TD")
        val shortIds = HashMap<String, String>()
        flow.nodes.forEachIndexed { index, node ->
            val id = "n$index"
            shortIds[node.id] = id
            val shape = when (node.type) {
                FlowNodeType.DECISION -> "{${escape(node.condition ?: node.name ?: node.id)}}"
                FlowNodeType.ACTION -> "([\"${escape(node.name ?: node.id)}\"])"
                FlowNodeType.EXTERNAL -> "([\"${escape(node.name ?: node.id)}\"])"
                else -> "[\"${escape(node.name ?: node.id)}\"]"
            }
            builder.appendLine("    $id$shape")
        }
        flow.edges.forEach { edge ->
            val from = shortIds[edge.from] ?: return@forEach
            val to = shortIds[edge.to] ?: return@forEach
            val label = listOfNotNull(edge.condition, edge.label)
                .joinToString(" / ")
                .takeIf { it.isNotBlank() }
            val arrow = if (label != null) "--|${escape(label)}|" else "-->"
            builder.appendLine("    $from$arrow$to")
        }
        return builder.toString()
    }

    private fun escape(text: String): String =
        text.replace("\"", "'")
            .replace("\n", " ")
            .replace("\r", " ")
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
}