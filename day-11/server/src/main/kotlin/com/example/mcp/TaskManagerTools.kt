package com.example.mcp

import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.PromptMessageContent
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredTool
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date


fun createTaskManagerTools(): List<RegisteredTool> {
    return listOf(
        RegisteredTool(
            Tool(
                name = "add_task",
                title = "Add task",
                description = "Add task to task manager system.",
                inputSchema = Tool.Input(
                    properties = JsonObject(
                        content = mapOf(
                            "content" to JsonObject(
                                mapOf(
                                    "type" to JsonPrimitive("string"),
                                    "description" to JsonPrimitive("Content of task"),
                                )
                            ),
                            "deadLine" to JsonObject(
                                mapOf(
                                    "type" to JsonPrimitive("string"),
                                    "description" to JsonPrimitive("DeadLine time of task in ISO8601 format"),
                                )
                            ),
                        )
                    ),
                    required = listOf("content", "deadLine")
                ),
                annotations = null,
                outputSchema = null,
            ),
            handler = ::handleAddTask
        ),
        RegisteredTool(
            Tool(
                name = "get_tasks",
                title = "Get tasks",
                description = "Returns planed tasks for a specific day",
                inputSchema = Tool.Input(
                    properties = JsonObject(
                        content = mapOf(
                            "day" to JsonObject(
                                mapOf(
                                    "type" to JsonPrimitive("string"),
                                    "description" to JsonPrimitive("Day in ISO8601 format"),
                                )
                            ),
                        )
                    ),
                    required = listOf("day")
                ),

                annotations = null,
                outputSchema = null,
            ),
            handler = ::handleGetTasks
        ),
    )
}

private suspend fun handleAddTask(callToolRequest: CallToolRequest): CallToolResult {
    val content = callToolRequest.arguments["content"]?.jsonPrimitive?.content
    val triggerTime = callToolRequest.arguments["deadLine"]?.jsonPrimitive?.content
    val errors = mutableListOf<PromptMessageContent>()
    if (content.isNullOrEmpty()) {
        errors.add(TextContent("Ошибка. Напоминание пустое"))
    }

    if (triggerTime.isNullOrEmpty()) {
        errors.add(TextContent("Ошибка. Время для напоминания пустое"))
    }

    if (errors.isNotEmpty()) {
        return CallToolResult(
            content = errors,
            isError = true,
        )
    }

    val task = Task(
        content = content.orEmpty(),
        triggerTime = triggerTime.orEmpty(),
    )
    TaskManager().addTask(task)
    println("Задача $content добавлена")
    return simpleTextContent("Уведомление успешно добавлено")
}

private suspend fun handleGetTasks(callToolRequest: CallToolRequest): CallToolResult {
    val date = callToolRequest.arguments["day"]?.jsonPrimitive?.content.orEmpty()
    if (date.isEmpty()) {
        return simpleTextContent("У вас нет списка дел на сегодня")
    }
    val localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
    val zonedDateTime = localDate.atStartOfDay(ZoneOffset.UTC)
    val dateStart = Date.from(zonedDateTime.toInstant())
    dateStart.hours = 0
    dateStart.minutes = 0
    dateStart.seconds = 0

    val dateEnd = Date.from(zonedDateTime.toInstant())
    dateEnd.hours = 23
    dateEnd.minutes = 59
    dateEnd.seconds = 59

    val reminders = TaskManager().getActualTasks(dateStart, dateEnd)
    if (reminders.isEmpty()) {
        return simpleTextContent("У вас нет списка дел на сегодня")
    }

    val jsonStr = Json.encodeToString(reminders)
    val jsonElement = Json.parseToJsonElement(jsonStr)
    return CallToolResult(
        content = listOf(TextContent("Ваш список дел на сегодня: $jsonStr")),
        structuredContent = buildJsonObject {
            put("items", jsonElement)
        }
    )
}