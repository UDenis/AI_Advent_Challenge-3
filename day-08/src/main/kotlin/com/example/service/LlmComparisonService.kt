package com.example.service

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import com.example.model.ApiResult
import com.example.model.LLMChatRequestDto.Message
import com.example.output.OutputFormatter
import kotlin.time.measureTimedValue

/**
 * Основной сервис для параллельной отправки запросов к LLM API
 */
class LlmComparisonService(
    private val deepSeekService: DeepSeekService,
) {
    private val contextLength = 128_000

    val tokenizer: HuggingFaceTokenizer by lazy {
        HuggingFaceTokenizer.newInstance("deepseek-ai/DeepSeek-V3.2-Exp")
    }


    private var history: List<Message> = emptyList()

    fun systemPromt(prompt: String) {
        history = history + Message.system(prompt)
    }

    /**
     * Отправляет промт параллельно в оба API и возвращает результаты
     */
    suspend fun sendMessage(
        prompt: String,
        summariseLongContext: Boolean = false,
    ): LlmComparisonResult {
        val userMessage = Message.user(prompt)

        val newHistory = if (summariseLongContext) {
            summariseIfLong(history + userMessage) + userMessage
        } else {
            history + userMessage
        }

        val measureResult = measureTimedValue { deepSeekService.generateResponse(newHistory) }
        when (val apiResult = measureResult.value) {
            is ApiResult.Error -> throw RuntimeException(apiResult.message)
            is ApiResult.Success -> {
                history =
                    newHistory + Message.assistant(apiResult.content.choices[0].message.content)
            }
        }

        return LlmComparisonResult(
            result = measureResult.value,
            executionTimeMs = measureResult.duration.inWholeMilliseconds,
        )
    }

    private suspend fun summariseIfLong(newMessages: List<Message>): List<Message> {
        return if (newMessages.isLong()) {
            println("Если мы отправим второе сообщение, то контекст не влезет в лимиты.")
            println("Поэтому подведим итог по нашему диалогу. Выдяляем факты и суть")
            val apiResult = deepSeekService.generateResponse(
                history + Message.user(
                    "Подведи итог нашему диалогу. Запомним факты, имена. Выдели основные темы, ключевые выводы и заключения." +
                            "Резюме должно быть кратким и структурированным."
                )
            )
            when (apiResult) {
                is ApiResult.Error -> throw RuntimeException(apiResult.message)
                is ApiResult.Success -> {
                    println(
                        "Сжатый контекст: (totalTokens:${apiResult.content.usage.totalTokens}," +
                                " promptTokens:${apiResult.content.usage.promptTokens}," +
                                " completionTokens:!! ${apiResult.content.usage.completionTokens} !!):"
                    )
                    println("     - ${apiResult.content.choices[0].message.content}")
                    println()
                    listOf(
                        Message.assistant(apiResult.content.choices[0].message.content)
                    )
                }
            }
        } else {
            history
        }
    }

    private fun List<Message>.isLong(): Boolean {
        var tokenCount = 0
        var isLong = false
        forEach {
            tokenCount += tokenizer.encode(it.content).ids.size
            if (tokenCount > contextLength) {
                isLong = true
                return@forEach
            }
        }

        return isLong
    }

    fun clearHistory() {
        history = emptyList()
    }
}

/**
 * Результат сравнения ответов от двух LLM API
 */
data class LlmComparisonResult(
    val result: ApiResult,
    val executionTimeMs: Long,
)
