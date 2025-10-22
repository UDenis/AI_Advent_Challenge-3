package com.example.model

import kotlinx.serialization.Serializable

/**
 * Ответ от Z.ai API
 */
@Serializable
data class ZAiResponseDto(
    val choices: List<Choice>,
    val usage: Usage,
    val model: String,
) {
    @Serializable
    data class Choice(
        val message: Message
    )

    @Serializable
    data class Usage(
        val prompt_tokens: Int,
        val completion_tokens: Int,
        val total_tokens: Int,
    )

    @Serializable
    data class Message(
        val content: String
    )
}


/**
 * Ответ от DeepSeek API
 */
@Serializable
data class DeepSeekResponseDto(
    val choices: List<Choice>,
    val usage: Usage,
    val model: String,
) {
    @Serializable
    data class Choice(
        val message: Message
    )

    @Serializable
    data class Usage(
        val prompt_tokens: Int,
        val completion_tokens: Int,
        val total_tokens: Int,
    )

    @Serializable
    data class Message(
        val content: String
    )
}

/**
 * Ответ от DeepSeek API
 */
@Serializable
data class HuggingfaceResponseDto(
    val choices: List<Choice>,
    val usage: Usage,
    val model: String,
) {
    @Serializable
    data class Choice(
        val message: Message
    )

    @Serializable
    data class Usage(
        val prompt_tokens: Int,
        val completion_tokens: Int,
        val total_tokens: Int,
    )

    @Serializable
    data class Message(
        val content: String
    )
}

/**
 * Результат выполнения запроса к API
 */
sealed class ApiResult {
    data class Success(val content: LLMResponse) : ApiResult()
    data class Error(val message: String) : ApiResult()
}
