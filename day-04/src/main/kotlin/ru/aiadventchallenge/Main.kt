package ru.aiadventchallenge

import kotlinx.coroutines.delay
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


        val temperature = arrayOf(0f, .3f, .7f, 1f)
        //val topP = arrayOf(1f, .7f, .3f, 0f)
        for (t in temperature) {
            val reply = chat.singleQuestion(
                temperature = t,
                userInput = userInput,
                topP = t,
            )
            printChatResponse(reply)
            print("\n")
            print("\n")
            delay(10_000)
        }
        println("До свидания! Приходите ещё")
    } catch (ex: AIResponseException) {
        println("Упс: что-то сломалось")
    }
}

private fun printChatResponse(reply: ChatResponse) {
    println("AI (t=${reply.usedTempereture}, topP=${reply.usedTopP}) : ${reply.message}")
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

