package com.example.service

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.GraphAIAgent
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.executor.clients.deepseek.DeepSeekLLMClient
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Сервис для работы с LLM через Koog
 */
class DeepSeekLlmService(
    val apiKey: String
) {

    /**
     * Создает агента для работы с OpenAI
     */
    private fun createOpenAIAgent(answerMode: AnswerMode): AIAgent<String, String> {
        return AIAgent(
            promptExecutor = SingleLLMPromptExecutor(
                llmClient = DeepSeekLLMClient(
                    apiKey = apiKey,
                )
            ),
            temperature = 0.5,
            systemPrompt = answerMode.prompt,
            llmModel = createLLMModel()
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


    /**
     * Генерирует ответ через OpenAI
     */
    suspend fun generateWithOpenAI(prompt: String, answerMode: AnswerMode): String {
        return withContext(Dispatchers.IO) {
            val agent = createOpenAIAgent(answerMode)
            agent.run("$prompt ${answerMode.prompt}")
        }
    }
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

enum class AnswerMode(
    val alias: String,
    val prompt: String,
) {
    SHORT(
        alias = "краткого",
        prompt = "Ты решаешь задачи. Выдавай краткий ясный ответ, без объяснений",
    ),
    FULL(
        alias = "полного",
        prompt = "Ты решаешь задачи. Выдавай полный развернутый ответ. Покажи ход рассуждений",
    )
}

