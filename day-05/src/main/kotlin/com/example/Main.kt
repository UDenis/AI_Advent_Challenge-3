package com.example

import com.example.config.ApiConfig
import com.example.output.OutputFormatter
import com.example.service.*
import kotlinx.coroutines.runBlocking
import java.util.Scanner

/**
 * Главная функция приложения
 * 
 * Приложение принимает промт от пользователя через стандартный ввод,
 * отправляет его параллельно в Z.ai и DeepSeek API,
 * и выводит результаты в терминал.
 */
fun main() = runBlocking {
    try {
        // Инициализация компонентов
        val (zaiApiKey, deepSeekApiKey, huggingfaceApiKey) = ApiConfig.getApiKeys()
        val httpClient = ApiConfig.createHttpClient()
        
        val zAiService = ZAiService(httpClient, zaiApiKey)
        val deepSeekService = DeepSeekService(httpClient, deepSeekApiKey)
        val huggingfaceService = HuggingfaceService(httpClient, huggingfaceApiKey)

        val comparisonService = LlmComparisonService(zAiService, deepSeekService, huggingfaceService)

        val scanner = Scanner(System.`in`, "UTF-8")

        // Приветственное сообщение
        OutputFormatter.printWelcome()
        // Запрос промта от пользователя
        print("\nВведите ваш запрос: ")
        val prompt = scanner.nextLine().trim()
        
        if (prompt.isBlank()) {
            println("Ошибка: Пустой запрос. Приложение завершает работу.")
            return@runBlocking
        }
        
        println("Отправляем запрос...")
        
        // Параллельная отправка запросов и получение результатов
        val results = comparisonService.compareResponses(prompt)
        
        // Вывод результатов
        OutputFormatter.printResults(results)
        
        // Завершение работы
        OutputFormatter.printGoodbye()
        
    } catch (e: IllegalStateException) {
        // Ошибка конфигурации (отсутствующие API ключи)
        println("Ошибка конфигурации: ${e.message}")
        println("Убедитесь, что установлены переменные окружения ZAI_API_KEY и DEEPSEEK_API_KEY")
    } catch (e: Exception) {
        // Общие ошибки
        println("Произошла неожиданная ошибка: ${e.message}")
        e.printStackTrace()
    }
}
