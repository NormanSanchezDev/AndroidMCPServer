package client

import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.StdioClientTransport
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered

fun main(): Unit = runBlocking {

    println("1. STARTING SERVER")
    val process = ProcessBuilder(
        "/Volumes/MacSSD/MCP's/Android/AndroidMasterMCP/build/install/AndroidMasterMCP/bin/AndroidMasterMCP"
    ).redirectError(ProcessBuilder.Redirect.INHERIT).start()
    println("2. SERVER PROCESS STARTED")
    val transport = StdioClientTransport(
        input = process.inputStream.asSource().buffered(),
        output = process.outputStream.asSink().buffered()
    )
    println("3. TRANSPORT CREATED")
    val client = Client(
        clientInfo = Implementation(
            name = "lattice-android-mcp-test-client",
            version = "0.1.0"
        )
    )
    println("4. MCP CONNECTED")
    try {
        client.connect(transport)
        println("CONNECTED")
        val tools = client.listTools()
        println("TOOLS:")
        tools.tools.forEach {
            println("- ${it.name}")
        }
    } finally {
        process.destroy()
    }
}