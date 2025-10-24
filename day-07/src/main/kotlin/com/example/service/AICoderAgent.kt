package com.example.service

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.GraphAIAgent
import ai.koog.agents.core.agent.entity.AIAgentNode
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.executor.clients.deepseek.DeepSeekLLMClient
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import aws.smithy.kotlin.runtime.telemetry.trace.Tracer


/**
 * Создает агента для работы с OpenAI
 */
internal fun createCoderAIAgent(
    apiKey: String,
): AIAgent<String, AICodeResult> {
    return AIAgent(
        promptExecutor = SingleLLMPromptExecutor(
            llmClient = DeepSeekLLMClient(
                apiKey = apiKey,
            )
        ),
        temperature = 0.5,
        llmModel = createLLMModel(),
        strategy = createCoderStrategy(),
    ) {
        //logLLMCall()
    }
}

private fun createLLMModel(): LLModel {
    return LLModel(
        id = "deepseek-chat",
        provider = LLMProvider.DeepSeek,
        capabilities = listOf(
            LLMCapability.Completion,
            LLMCapability.OpenAIEndpoint.Completions,
            LLMCapability.Temperature,
        ),
        contextLength = 128_000,
        maxOutputTokens = 4000,
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
