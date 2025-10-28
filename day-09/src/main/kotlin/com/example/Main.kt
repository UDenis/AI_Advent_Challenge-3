package com.example

import com.example.config.ApiConfig
import com.example.service.createAIAgent
import com.example.service.createAIAgent2
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

//        val aiAgent = createAIAgent2(
//            apiKey = ApiConfig.ZAI_API_KEY(),
//            useMcp = false,
//        )
//        val calculatorAgent = createAIAgent2(
//            apiKey = ApiConfig.ZAI_API_KEY(),
//            useMcp = false,
//        )
        //val scanner = Scanner(System.`in`, "UTF-8")

        // Запрос промта от пользователя
        println("Задай мне примерчик и я его решу:")
        //val prompt = scanner.nextLine().trim()
        //val prompt = "Сколько будет (10*(3+20/5)-30)/10-10"
        val prompt = "Сколько будет ((12-76)/81/(64/9))*(9/37)"
        println("Задача: $prompt")
        println("---- PS: Ответе должен быть -1/37=-0.27")
        println()
        if (prompt.isBlank()) {
            println("Ошибка: Пустой запрос. Приложение завершает работу.")
            return@runBlocking
        }

//        println("\nРешает простой агент:")
//        var result = aiAgent.run(prompt)
//        println("Результат:")
//        println("         :${result}")
//        println()

        println("\nРешает агент с подключенныйм MCP `калькулятор`")
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
