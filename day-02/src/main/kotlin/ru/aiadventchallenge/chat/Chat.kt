package ru.aiadventchallenge.chat

import ai.z.openapi.service.model.ChatMessage
import kotlinx.serialization.json.Json
import ru.aiadventchallenge.aiclient.AIClient
import ru.aiadventchallenge.aiclient.createAssistantMessage
import ru.aiadventchallenge.aiclient.createSystemMessage
import ru.aiadventchallenge.aiclient.createUserMessage
import ru.aiadventchallenge.chat.exception.AIResponseException
import ru.aiadventchallenge.chat.model.ChatResponse

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
    fun initialize(): ChatResponse {
        messages.add(
            createSystemMessage("""
                Respond with valid JSON only in the format:
                {
                  "message": "<Your answer to the question. Should be string>",
                  "usedTokens": <Used tokens count. Should be integer>
                }
            """.trimIndent())
        )
        messages.add(
            createUserMessage("Привет! Представься и скажи, что ты готов помочь.")
        )
        return callAI()
    }

    /**
     * Отправляет сообщение пользователя и получает ответ от AI
     * @return Ответ от AI
     */
    fun sendMessage(userInput: String): ChatResponse {
        // Добавляем сообщение пользователя в историю
        messages.add(createUserMessage(userInput))
        return callAI()
    }

    private fun callAI(): ChatResponse {
        print("AI печатает ....")
        // Отправляем запрос
        val response = zaiService.createChatCompletion(
            model = model,
            messages = messages
        )
        print("\r${" ".repeat(50)}\r")
        System.out.flush()
        if (response.isSuccess.not()) {
            throw AIResponseException()
        }
        // Обрабатываем ответ
        val reply = response.data.choices[0].message.content
        val jsonString = reply.toString()
        println("DEBUG: Raw response: $jsonString")
        messages.add(createAssistantMessage(jsonString))
        
        // Парсим JSON ответ в ChatResponse объект
        return Json.decodeFromString<ChatResponse>(jsonString)
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
