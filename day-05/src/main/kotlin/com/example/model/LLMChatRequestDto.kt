package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class LLMChatRequestDto(
    val model: String,
    val temperature: Float,
    val max_tokens: Int,
    val messages: List<Message>,
    val stream: Boolean = false
) {
    @Serializable
    data class Message(
        val role: String,
        val content: String,
    )
}
