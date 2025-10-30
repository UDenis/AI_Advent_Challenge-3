package com.example

import com.example.config.ApiConfig
import com.example.service.createAIAgent
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
        var aiAgent = createAIAgent(
            apiKey = ApiConfig.DEEPSEEK_API_KEY(),
        )

        // Запрос промта от пользователя
        println("Помогаю запоминать список дел:")
        //val prompt = scanner.nextLine().trim()
        //val prompt = "Сколько будет (10*(3+20/5)-30)/10-10"
        var prompt = "Сегодня в 12:30 встреча с президентом. Добавь информацию о встрече."

        println("Запрос: $prompt")
        println()
        if (prompt.isBlank()) {
            println("Ошибка: Пустой запрос. Приложение завершает работу.")
            return@runBlocking
        }

        println("\nАгент думает")
        var result = aiAgent.run(prompt)
        println("Результат:")
        println("         :${result}")

        prompt = "Напомни какие у меня встречи на сегодня?"
        println("Запрос: $prompt")
        println()
        println("\nАгент думает")
        aiAgent = createAIAgent(
            apiKey = ApiConfig.DEEPSEEK_API_KEY(),
        )
        result = aiAgent.run(prompt)
        println("Результат:")
        println("         :${result}")

    } catch (e: IllegalStateException) {
        // Ошибка конфигурации (отсутствующие API ключи)
        println("Ошибка конфигурации")
        e.printStackTrace()
    } catch (e: Exception) {
        // Общие ошибки
        println("Произошла неожиданная ошибка")
        e.printStackTrace()
    }
}
