package com.example.service

import com.example.model.ApiResult
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlin.time.measureTimedValue

/**
 * Основной сервис для параллельной отправки запросов к LLM API
 */
class LlmComparisonService(
    private val zAiService: ZAiService,
    private val deepSeekService: DeepSeekService,
    private val huggingfaceService: HuggingfaceService,
) {

    /**
     * Отправляет промт параллельно в оба API и возвращает результаты
     */
    suspend fun compareResponses(prompt: String): List<LlmComparisonResult> {
        return coroutineScope {
            // Запускаем запросы параллельно
            val zAiDeferred = async {
                measureTimedValue { zAiService.generateResponse(prompt) }
                    .let {
                        LlmComparisonResult(
                            service = "Z.ai",
                            result = it.value,
                            executionTimeMs = it.duration.inWholeMilliseconds
                        )
                    }
            }
            val deepSeekDeferred = async {
                measureTimedValue { deepSeekService.generateResponse(prompt) }
                    .let {
                        LlmComparisonResult(
                            service = "deepseek.com",
                            result = it.value,
                            executionTimeMs = it.duration.inWholeMilliseconds
                        )
                    }
            }

            listOf(
                "mistralai/Mistral-7B-Instruct-v0.2:featherless-ai",
                "Qwen/QwQ-32B:nscale",
                "meta-llama/Meta-Llama-3-8B-Instruct",
            ).map {
                measureTimedValue {
                    huggingfaceService.generateResponse(
                        model = it,
                        prompt = prompt
                    )
                }
                    .let {
                        delay(4000)
                        LlmComparisonResult(
                            service = "Huggingface.co",
                            result = it.value,
                            executionTimeMs = it.duration.inWholeMilliseconds
                        )
                    }
            } + listOf(
                zAiDeferred.await(),
                deepSeekDeferred.await(),
            )
        }
    }
}

/**
 * Результат сравнения ответов от двух LLM API
 */
data class LlmComparisonResult(
    val result: ApiResult,
    val executionTimeMs: Long,
    val service: String,
)
