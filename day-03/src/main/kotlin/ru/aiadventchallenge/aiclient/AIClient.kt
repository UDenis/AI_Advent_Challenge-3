package ru.aiadventchallenge.aiclient

import ai.z.openapi.ZaiClient
import ai.z.openapi.service.model.ChatCompletionCreateParams
import ai.z.openapi.service.model.ChatMessage
import java.util.Arrays

/**
 * Сервис для работы с ZaiClient API
 */
class AIClient(
    apiKey: String
) {
    private val client: ZaiClient = ZaiClient.builder()
        .apiKey(apiKey)
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
        messages: List<ChatMessage>
    ): ai.z.openapi.service.model.ChatCompletionResponse {
        val request = ChatCompletionCreateParams.builder()
            .model(model)
            .messages(Arrays.asList(*messages.toTypedArray()))
            .build()

        return client.chat().createChatCompletion(request)
    }
}
