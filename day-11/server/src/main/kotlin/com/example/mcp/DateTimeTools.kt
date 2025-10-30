package com.example.mcp

import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.Tool.Input
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredTool
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.putJsonObject
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun createDateTimeTools(): List<RegisteredTool> {
    return listOf(
        RegisteredTool(
            tool = Tool(
                name = "date-time",
                title = "Date time",
                description = "Return datetime in ISO8601 format",
                inputSchema = Tool.Input(
                    properties = JsonObject(
                        content = mapOf(
                            "expression" to JsonObject(
                                mapOf(
                                    "type" to JsonPrimitive("string"),
                                    "description" to JsonPrimitive("May be today or tomorrow value"),
                                )
                            ),
                        )
                    ),
                    required = listOf("expression")
                ),
                outputSchema = null,
                annotations = null,
            ),
            handler = ::getDateTime
        )
    )

}

private suspend fun getDateTime(callToolRequest: CallToolRequest): CallToolResult {
    val expression = callToolRequest.arguments["expression"]?.jsonPrimitive?.content
    return when (expression) {
        "today" -> simpleTextContent(
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
                ZonedDateTime.now()
            )
        )

        "tomorrow" -> simpleTextContent(
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
                ZonedDateTime.now().plusDays(1)
            )
        )

        else -> {
            CallToolResult(
                content = listOf(TextContent("Invalid expression")),
                isError = true,
            )
        }
    }
}