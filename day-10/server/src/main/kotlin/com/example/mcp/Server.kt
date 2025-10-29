package com.example.mcp

import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.sse.ServerSSESession
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

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
            name = "mcp-kotlin test server",
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

    val weatherClient = OpenMeteoAPIClient()

    server.addTool(
        name = "weather",
        description = "Get weather information using Open-Meteo API",
        title = "Weather",
        inputSchema = Tool.Input(
            properties = JsonObject(
                content = mapOf(
                    "latitude" to JsonObject(mapOf("type" to JsonPrimitive("number"))),
                    "longitude" to JsonObject(mapOf("type" to JsonPrimitive("number"))),
                )
            ),
            required = listOf("latitude", "longitude")
        ),
        //outputSchema =
    ) { request ->
        val latitude = request.arguments["latitude"]?.jsonPrimitive?.content?.toDoubleOrNull()
        val longitude = request.arguments["longitude"]?.jsonPrimitive?.content?.toDoubleOrNull()

        if (latitude == null || longitude == null) {
            throw IllegalArgumentException("Latitude and longitude must be provided as numbers")
        }

        println("Обрабатываем weather.tool call ($latitude, $longitude)")
        val weather = weatherClient.getWeather(latitude, longitude)
        println("Responce $weather")
        // Преобразуем в JsonObject для ответа
        val json = JsonObject(
            mapOf(
                "latitude" to JsonPrimitive(weather.latitude),
                "longitude" to JsonPrimitive(weather.longitude),
                "elevation" to JsonPrimitive(weather.elevation),
                "timezone" to JsonPrimitive(weather.timezone),
                "hourly_times" to JsonObject(
                    weather.hourly.time.mapIndexed { index, time ->
                        time to JsonObject(
                            mapOf(
                                "temperature_2m" to JsonPrimitive(weather.hourly.temperature_2m[index]),
                                "relative_humidity_2m" to JsonPrimitive(weather.hourly.relative_humidity_2m[index]),
                                "precipitation" to JsonPrimitive(weather.hourly.precipitation[index]),
                                "weather_code" to JsonPrimitive(weather.hourly.weather_code[index])
                            )
                        )
                    }.toMap()
                )
            )
        )

        CallToolResult(
            structuredContent = json,
            content = listOf(
                TextContent("$weather")
            )
        )
    }

    return server
}