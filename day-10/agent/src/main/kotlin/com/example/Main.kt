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
        val aiAgent = createAIAgent(
            apiKey = ApiConfig.DEEPSEEK_API_KEY(),
            useMcp = false,
        )
        val calculatorAgent = createAIAgent(
            apiKey = ApiConfig.DEEPSEEK_API_KEY(),
            useMcp = true,
        )

        // Запрос промта от пользователя
        println("Можешь спросить меня о погоде:")
        //val prompt = scanner.nextLine().trim()
        //val prompt = "Сколько будет (10*(3+20/5)-30)/10-10"
        val prompt = "Какая сейчас погода в Уфе. Координаты 54°44′ с. ш. 55°58′ в. д."
        println("Задача: $prompt")
        println()
        if (prompt.isBlank()) {
            println("Ошибка: Пустой запрос. Приложение завершает работу.")
            return@runBlocking
        }

        println("\nАгент думает")
        val result = calculatorAgent.run(prompt)
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
