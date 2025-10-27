package com.example.model

data class LLMResponse(
    val choices: List<Choice>,
    val usage: Usage,
    val model: String,
) {
    data class Choice(
        val message: Message
    )

    data class Usage(
        val promptTokens: Int,
        val completionTokens: Int,
        val totalTokens: Int,
        val cost: Double,
    )

    data class Message(
        val content: String
    )
}