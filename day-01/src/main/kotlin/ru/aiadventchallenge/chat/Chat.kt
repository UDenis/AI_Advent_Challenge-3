package ru.aiadventchallenge.chat

import ai.z.openapi.service.model.ChatMessage
import ru.aiadventchallenge.chat.exception.AIResponseException
import ru.aiadventchallenge.aiclient.AIClient
import ru.aiadventchallenge.aiclient.createAssistantMessage
import ru.aiadventchallenge.aiclient.createUserMessage

/**
 * Класс для управления чатом с AI
 */
class Chat(
    private val zaiService: AIClient,
    private val model: String = "glm-4.6"
) {
    private val messages = mutableListOf<ChatMessage>()

    /**
     * Инициализирует чат приветственным сообщением
     * @return Welcome message from AI
     */
    suspend fun initialize(): String =
        sendMessage("Привет! Представься и скажи, что ты готов помочь.")

    /**
     * Отправляет сообщение пользователя и получает ответ от AI
     * @return Ответ от AI
     */
    suspend fun sendMessage(userInput: String): String {
        // Добавляем сообщение пользователя в историю
        messages.add(createUserMessage(userInput))

        // Отправляем запрос
        val response = zaiService.createChatCompletion(
            model = model,
            messages = messages
        )

        if (response.isSuccess.not()) {
            throw AIResponseException()
        }

        // Обрабатываем ответ
        val reply = response.data.choices[0].message.content
        messages.add(createAssistantMessage(reply.toString()))
        return messages.last().content.toString()
    }

    /**
     * Получает количество сообщений в истории
     */
    fun getMessageCount(): Int = messages.size

    /**
     * Очищает историю сообщений
     */
    fun clearHistory() {
        messages.clear()
    }
}
