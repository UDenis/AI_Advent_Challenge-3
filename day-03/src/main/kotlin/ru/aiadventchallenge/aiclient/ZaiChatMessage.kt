package ru.aiadventchallenge.aiclient

import ai.z.openapi.service.model.ChatMessage
import ai.z.openapi.service.model.ChatMessageRole

/**
 * Создает сообщение пользователя
 *
 * @param content Содержимое сообщения
 * @return ChatMessage объект
 */
fun createUserMessage(content: String): ChatMessage {
    return ChatMessage.builder()
        .role(ChatMessageRole.USER.value())
        .content(content)
        .build()
}

/**
 * Создает сообщение ассистента
 *
 * @param content Содержимое сообщения
 * @return ChatMessage объект
 */
fun createAssistantMessage(content: String): ChatMessage {
    return ChatMessage.builder()
        .role(ChatMessageRole.ASSISTANT.value())
        .content(content)
        .build()
}

/**
 * Создает системное сообщение
 *
 * @param content Содержимое сообщения
 * @return ChatMessage объект
 */
fun createSystemMessage(content: String): ChatMessage {
    return ChatMessage.builder()
        .role(ChatMessageRole.SYSTEM.value())
        .content(content)
        .build()
}