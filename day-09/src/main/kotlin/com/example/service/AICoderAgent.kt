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
        systemPrompt = "Ты ассистент помощник. Выдавай короткий ясный ответ на вопрос в виде десятичной дроби",
        llmModel = createDeepseekChatLLMModel(),
        toolRegistry = when (useMcp) {
            true -> calculatorToolRegistry()
            false -> ToolRegistry.EMPTY
        }
    ) {
        logLLMCall()
    }
}


internal suspend fun createAIAgent2(
    apiKey: String,
    useMcp: Boolean,
): AIAgent<String, String> {
    return AIAgent(
        promptExecutor = SingleLLMPromptExecutor(
            llmClient = OpenAILLMClient(
                apiKey = apiKey,
                settings = OpenAIClientSettings(
                    baseUrl = "https://api.z.ai",
                    chatCompletionsPath="/api/paas/v4/chat/completions",
                )
            )
        ),
        temperature = 0.5,
        systemPrompt = "Ты ассистент помощник. Выдавай короткий ясный ответ на вопрос в виде десятичной дроби",
        llmModel = createZAiGlmChatLLMModel(),
        toolRegistry = when (useMcp) {
            true -> calculatorToolRegistry()
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


private fun createZAiGlmChatLLMModel(): LLModel {
    return LLModel(
        id = "glm-4.6",
        provider = object :LLMProvider("z.ai", "Z.ai"){},
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


private suspend fun calculatorToolRegistry(): ToolRegistry {
    // Start the Calculator MCP server
    val process = ProcessBuilder(
        "docker", "run", "-i", "mcp-server-calculator"
    ).start()

    /// Create the ToolRegistry with tools from the MCP server
    return McpToolRegistryProvider.fromTransport(
        transport = McpToolRegistryProvider.defaultStdioTransport(process)
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
