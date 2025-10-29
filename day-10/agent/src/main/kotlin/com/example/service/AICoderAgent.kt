package com.example.service

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.GraphAIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.agents.mcp.McpToolRegistryProvider
import ai.koog.agents.mcp.defaultStdioTransport
import ai.koog.prompt.executor.clients.deepseek.DeepSeekLLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import io.ktor.client.HttpClient
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.SseClientTransport
import io.ktor.client.plugins.sse.*

internal suspend fun createAIAgent(
    apiKey: String,
    useMcp: Boolean,
): AIAgent<String, String> {
    return AIAgent(
        promptExecutor = SingleLLMPromptExecutor(
            llmClient = DeepSeekLLMClient(
                apiKey = apiKey,
            )
        ),
        temperature = 0.5,
        systemPrompt = "Ты ассистент помощник. Выдавай короткий ясный ответ",
        llmModel = createDeepseekChatLLMModel(),
        toolRegistry = when (useMcp) {
            true -> weatherToolRegistry()
            false -> ToolRegistry.EMPTY
        }
    ) {
        logLLMCall()
    }
}

private fun createDeepseekChatLLMModel(): LLModel {
    return LLModel(
        id = "deepseek-chat",
        provider = LLMProvider.DeepSeek,
        capabilities = listOf(
            LLMCapability.Completion,
            LLMCapability.OpenAIEndpoint.Completions,
            LLMCapability.Temperature,
            LLMCapability.Tools,
        ),
        contextLength = 128_000,
        maxOutputTokens = 4000,
    )
}

private suspend fun weatherToolRegistry(): ToolRegistry {
    /// Create the ToolRegistry with tools from the MCP server
    val mcpClient = Client(clientInfo = Implementation(name = "mcp-client-cli", version = "1.0.0"))
    mcpClient.connect(
        transport = SseClientTransport(
            client = HttpClient {
                install(SSE)
            },
            urlString = "http://localhost:3001"
        )
    )
    return McpToolRegistryProvider.fromClient(
        mcpClient = mcpClient
    )
}

private fun GraphAIAgent.FeatureContext.logLLMCall() {
    handleEvents {
        onLLMCallStarting { ctx ->
            println("Request to LLM:")
            println("    #Message:")
            ctx.prompt.messages.forEach {
                println("     -: $it")
            }
            println("    #Tools:")
            ctx.tools.forEach {
                println("     -: $it")
            }
            println("-------------------------------------")
        }

        onLLMCallCompleted { ctx ->
            println("Response from LLM:")
            println("    #Message:")
            ctx.responses.forEach {
                println("     -: $it")
            }
            println("-------------------------------------")
        }
    }
}
