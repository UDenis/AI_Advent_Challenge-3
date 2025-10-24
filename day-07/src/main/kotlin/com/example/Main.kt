package com.example

import com.example.config.ApiConfig
import com.example.service.createCoderAIAgent
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
        val apiKey = ApiConfig.getApiKeys()
        val aiAger = createCoderAIAgent(
            apiKey = apiKey,
        )
        val scanner = Scanner(System.`in`, "UTF-8")

        // Запрос промта от пользователя
        print("\nЯ умею прогать на Kotlin, возможно лучше чем ты.")
        print("\nОпиши задачку и я её решу: ")
        val prompt = scanner.nextLine().trim()

        //val prompt = "Напиши extension fun к List, которая реализует пузырьковую сортировку"
        //val prompt = "Напиши функцию возвращающая последовательность фибоначчи"
        //println("Задача: $prompt")
        println()
        if (prompt.isBlank()) {
            println("Ошибка: Пустой запрос. Приложение завершает работу.")
            return@runBlocking
        }

        val result = aiAger.run(prompt)
        println("Code:")
        println(result.code)

        println()
        println("====================================================")
        println()

        println("UnitTests:")
        println(result.unitTests)

        println()
        println("====================================================")
        println()

        println("Review:")
        println(result.review)

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
