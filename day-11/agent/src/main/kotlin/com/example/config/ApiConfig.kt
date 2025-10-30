package com.example.config

/**
 * Конфигурация API провайдеров
 */
object ApiConfig {
    /**
     * Получение API ключей из переменных окружения
     */
    fun DEEPSEEK_API_KEY(): String = apiKey("DEEPSEEK_API_KEY")

    fun ZAI_API_KEY(): String = apiKey("ZAI_API_KEY")

    private fun apiKey(name: String): String {
        val zaiKey = System.getenv(name)
            ?: throw IllegalStateException("$name environment variable is not set")

        return zaiKey
    }
}


