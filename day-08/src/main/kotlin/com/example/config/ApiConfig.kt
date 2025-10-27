package com.example.config

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

/**
 * Конфигурация API провайдеров
 */
object ApiConfig {
    // URL и модели для каждого провайдера
    const val ZAI_BASE_URL = "https://api.z.ai/api/paas/v4"
    const val ZAI_MODEL = "glm-4.6"
    const val ZAI_ENDPOINT = "$ZAI_BASE_URL/chat/completions"
    
    const val DEEPSEEK_BASE_URL = "https://api.deepseek.com/"
    const val DEEPSEEK_MODEL = "deepseek-chat"
    const val DEEPSEEK_ENDPOINT = "$DEEPSEEK_BASE_URL/chat/completions"


    const val HUGGINGFACE_BASE_URL = "https://router.huggingface.co/v1"
    const val HUGGINGFACE_ENDPOINT = "$HUGGINGFACE_BASE_URL/chat/completions"

    // Параметры генерации
    const val TEMPERATURE = 0.7f
    const val MAX_TOKENS = 2048
    
    /**
     * Получение API ключей из переменных окружения
     */
    fun getApiKeys(): String {
        val deepSeekKey = System.getenv("DEEPSEEK_API_KEY")
            ?: throw IllegalStateException("DEEPSEEK_API_KEY environment variable is not set")

        return deepSeekKey
    }
    
    /**
     * Создание HTTP клиента с настроенной сериализацией
     */
    fun createHttpClient(): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = false
                })
            }
            install(HttpTimeout) {
                // Global timeouts for all requests made by this client instance
                requestTimeoutMillis = 60.seconds.inWholeMilliseconds // Total request processing time
                //connectTimeoutMillis = 5.seconds.inWholeMilliseconds  // Time to establish a connection
                //socketTimeoutMillis = 10.seconds.inWholeMilliseconds  // Inactivity time between data packets
            }
//            install(Logging) {
//                level = LogLevel.INFO
//            }
        }
    }
}
