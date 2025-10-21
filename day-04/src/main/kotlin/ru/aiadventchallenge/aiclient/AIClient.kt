package ru.aiadventchallenge.aiclient

import ai.z.openapi.ZaiClient
import ai.z.openapi.service.model.ChatCompletionCreateParams
import ai.z.openapi.service.model.ChatMessage
import java.util.Arrays
import java.util.concurrent.TimeUnit

/**
 * Сервис для работы с ZaiClient API
 */
class AIClient(
    apiKey: String
) {
    private val client: ZaiClient = ZaiClient.builder()
        .apiKey(apiKey)
        //.networkConfig(60, 60, 20, 20, TimeUnit.SECONDS)
        .build()

    /**
     * Отправляет запрос на создание чат-комплита
     *
     * @param model Модель для использования
     * @param messages Список сообщений
     * @return Результат выполнения запроса
     */
    fun createChatCompletion(
        model: String,
        temperature: Float,
        messages: List<ChatMessage>
    ): ai.z.openapi.service.model.ChatCompletionResponse {
        val request = ChatCompletionCreateParams.builder()
            .temperature(temperature)
            .model(model)
            .messages(Arrays.asList(*messages.toTypedArray()))
            .build()

        return client.chat().createChatCompletion(request)
    }
}
