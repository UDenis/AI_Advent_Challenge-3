package com.example.mcp

import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.sse.ServerSSESession
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcp

suspend fun runSseMcpServerUsingKtor(port: Int) {
    println("Starting sse server on port $port")
    println("Use inspector to connect to the http://localhost:$port")

    embeddedServer(CIO, host = "127.0.0.1", port = port) {
        mcp {
            configureServer()
        }
    }.startSuspend(wait = true)
}

private fun ServerSSESession.configureServer(): Server {
    val server = Server(
        serverInfo = Implementation(
            name = "MCP server for my reminders",
            version = "0.1.0",
        ),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                prompts = ServerCapabilities.Prompts(listChanged = true),
                resources = ServerCapabilities.Resources(subscribe = true, listChanged = true),
                tools = ServerCapabilities.Tools(listChanged = true),
            ),
        ),
    )

    server.addTools(
        createTaskManagerTools() + createDateTimeTools()
    )
    return server
}

fun simpleTextContent(str: String) = CallToolResult(
    content = listOf(TextContent(str)),
)