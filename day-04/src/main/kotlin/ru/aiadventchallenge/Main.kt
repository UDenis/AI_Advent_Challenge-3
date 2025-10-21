package ru.aiadventchallenge

import kotlinx.coroutines.runBlocking
import ru.aiadventchallenge.aiclient.AIClient
import ru.aiadventchallenge.chat.Chat
import ru.aiadventchallenge.chat.exception.AIResponseException
import ru.aiadventchallenge.chat.model.ChatResponse
import java.util.Scanner

fun main(): Unit = runBlocking {
    try {
        val apiKey = System.getenv("ZAI_API_KEY")
            ?: throw IllegalStateException("ZAI_API_KEY env variable not found")
        val chat = Chat(AIClient(apiKey))
        val scanner = Scanner(System.`in`, "UTF-8")

        println("Познаем на практике как параметр temperature влияет на выдачу")
        //val userInput = "Я сегодня грустный. Напиши одно предложение, которое убедит меня взять сегодня с собой зонт."
        val userInput = "Меня зовут Денис. Я грустный ребёнок. Напиши одно предложение, которое убедит меня взять сегодня с собой зонт."
        println(userInput)
        // Проверяем, что пользователь ввел что-то
//        if (isEmptyInput(userInput)) {
//            println("Вы ничего не ввели. Пока")
//            return@runBlocking
//        }


        val temperature = arrayOf(0f, .2f, .5f, .7f, .9f, 1f)
        for (t in temperature) {
            val reply = chat.singleQuestion(
                temperature = t,
                userInput = userInput,
            )
            printChatResponse(reply)
            print("\n")
            print("\n")
        }
        println("До свидания! Приходите ещё")
    } catch (ex: AIResponseException) {
        println("Упс: что-то сломалось")
    }
}

private fun printChatResponse(reply: ChatResponse) {
    println("AI (t=${reply.userTempereture}: ${reply.message}")
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

