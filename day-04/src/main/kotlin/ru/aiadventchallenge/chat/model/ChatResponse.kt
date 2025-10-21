package ru.aiadventchallenge.chat.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatResponse(
    val message: String,
    val usedTempereture: Float,
    val usedTopP: Float,
)