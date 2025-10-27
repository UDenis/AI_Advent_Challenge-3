package com.example.service

import com.example.config.ApiConfig
import com.example.model.ApiResult
import com.example.model.DeepSeekResponseDto
import com.example.model.LLMChatRequestDto
import com.example.model.LLMChatRequestDto.Message
import com.example.model.LLMResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Сервис для работы с DeepSeek API
 */
class DeepSeekService(private val httpClient: HttpClient, private val apiKey: String) {
    suspend fun generateResponse(messages: List<Message>): ApiResult {
        return withContext(Dispatchers.IO) {
            val httpResponse: HttpResponse = httpClient.post(ApiConfig.DEEPSEEK_ENDPOINT) {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(
                    LLMChatRequestDto(
                        model = ApiConfig.DEEPSEEK_MODEL,
                        temperature = ApiConfig.TEMPERATURE,
                        max_tokens = ApiConfig.MAX_TOKENS,
                        messages = messages,
                    )
                )
            }

            when (httpResponse.status) {
                HttpStatusCode.OK -> {
                    val response: DeepSeekResponseDto = httpResponse.body()
                    ApiResult.Success(
                        LLMResponse(
                            model = response.model, usage = LLMResponse.Usage(
                                promptTokens = response.usage.prompt_tokens,
                                completionTokens = response.usage.completion_tokens,
                                totalTokens = response.usage.total_tokens,
                                cost = (response.usage.prompt_tokens * 0.028 / 1000_000 + response.usage.completion_tokens * 0.28 / 1000_000)
                            ), choices = response.choices.map {
                                LLMResponse.Choice(
                                    message = LLMResponse.Message(it.message.content)
                                )
                            }

                        ))
                }

                else -> {
                    val errorBody: String = httpResponse.bodyAsText()!!
                    ApiResult.Error(errorBody)
                }
            }
        }
    }
}


