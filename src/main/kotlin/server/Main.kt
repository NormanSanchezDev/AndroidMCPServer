package dev.lattice.androidmcp.server

import latticeMCP.tools.ProjectInspectTool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.types.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

fun main(args: Array<String>) {
    val port = args.firstOrNull()?.toIntOrNull() ?: 3002
    val mcpServer = Server(
        serverInfo = Implementation(
            name = "lattice-android-mcp-server", version = "1.0.0"
        ), options = ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = true),
            ),
        )
    )

    mcpServer.addTool(
        name = "android.project.inspect", description = """
        Inspect a local project path and return deterministic evidence
        about whether it exists and whether it is a Gradle project.
    """.trimIndent(), inputSchema = ToolSchema(
        properties = buildJsonObject {
            put(
                "input", buildJsonObject {
                    put("type", "string")
                    put("description", "Absolute path to the Android project root")
                })
        })
    ) { request ->
        val projectRoot = request.arguments
            ?.get("projectRoot")
            ?.jsonPrimitive
            ?.content
        if (projectRoot.isNullOrBlank()) {
            return@addTool CallToolResult(
                content = listOf(
                    TextContent(
                        text = """{"status":"error","error":"projectRoot is required"}"""
                    )
                ),
                isError = true
            )
        }
        val result = ProjectInspectTool.execute(projectRoot)
        CallToolResult(
            content = listOf(
                TextContent(
                    text = result.toString()
                )
            )
        )
    }
}