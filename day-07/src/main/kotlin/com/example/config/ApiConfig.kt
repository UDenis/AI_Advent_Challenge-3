package com.example.config

/**
 * Конфигурация API провайдеров
 */
object ApiConfig {
    /**
     * Получение API ключей из переменных окружения
     */
    fun getApiKeys(): String {

        val zaiKey = System.getenv("DEEPSEEK_API_KEY")
            ?: throw IllegalStateException("DEEPSEEK_API_KEY environment variable is not set")

        return zaiKey
    }
}
