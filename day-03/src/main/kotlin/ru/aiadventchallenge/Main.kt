package ru.aiadventchallenge

import kotlinx.coroutines.runBlocking
import ru.aiadventchallenge.aiclient.AIClient
import ru.aiadventchallenge.chat.AnimationType
import ru.aiadventchallenge.chat.Chat
import ru.aiadventchallenge.chat.LoadingIndicator
import ru.aiadventchallenge.chat.exception.AIResponseException
import ru.aiadventchallenge.chat.model.ChatResponse
import java.util.Scanner

fun main(): Unit = runBlocking {
    try {
        val apiKey = System.getenv("ZAI_API_KEY")
            ?: throw IllegalStateException("ZAI_API_KEY env variable not found")
        val chat = Chat(AIClient(apiKey))
        val scanner = Scanner(System.`in`, "UTF-8")

        println("Добро пожаловать в AI чат! Введите 'exit' для выхода.")

        // Инициализируем чат с эффектом загрузки
        val welcomeMessage = chat.initialize()
        printChatResponse(welcomeMessage)
        // Бесконечный цикл для интерактивного чата
        while (true) {
            print("\nВы: ")
            //val userInput = readLine()?.trim().orEmpty()
            val userInput = scanner.nextLine().trim()

            // Проверяем команду выхода
            if (isExitCommand(userInput)) {
                break
            }
            // Проверяем, что пользователь ввел что-то
            if (isEmptyInput(userInput)) {
                println("Пожалуйста, введите сообщение.")
                continue
            }

            // Отправляем сообщение с эффектом загрузки
            val reply = chat.sendMessage(userInput)
            printChatResponse(reply)
        }
        println("До свидания! Приходите ещё")
    } catch (ex: AIResponseException) {
        println("Упс: что-то сломалось")
    }
}

private fun printChatResponse(reply: ChatResponse) {
    println("AI: ${reply.message}")
}

/**
 * Проверяет, является ли ввод командой выхода
 */
fun isExitCommand(input: String?): Boolean {
    return input == "exit" || input == "выход"
}

/**
 * Проверяет, является ли ввод пустым
 */
fun isEmptyInput(input: String): Boolean {
    return input.isEmpty()
}

