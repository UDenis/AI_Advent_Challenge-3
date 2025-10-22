package com.example.output

import com.example.model.ApiResult
import com.example.service.LlmComparisonResult

/**
 * Сервис для форматированного вывода результатов сравнения LLM API
 */
object OutputFormatter {
    
    /**
     * Выводит результаты сравнения в заданном формате
     */
    fun printResults(results: List<LlmComparisonResult>) {
        results.forEach {
            println()
            println("Ответ от ${it.service}:")
            printApiResult(it.result)
        }
    }
    
    /**
     * Выводит результат от конкретного API
     */
    private fun printApiResult(result: ApiResult) {
        when (result) {
            is ApiResult.Success -> {
                println("-- model:${result.content.model}," +
                        " totalTokens:${result.content.usage.totalTokens}," +
                        " promptTokens:${result.content.usage.promptTokens}, " +
                        " completionTokens:${result.content.usage.completionTokens}, " +
                        " cost:${String.format("$%.6f",  result.content.usage.cost)}")
                println("${result.content.choices.firstOrNull()?.message?.content.orEmpty()}\" ")
                println("--------------------------------------")
            }
            is ApiResult.Error -> {
                println("Ошибка получения ответа.: ${result.message}")
            }
        }
    }
    
    /**
     * Выводит приветственное сообщение
     */
    fun printWelcome() {
        println("=== Сравнение LLM API ===")
        println("Приложение отправляет ваш запрос параллельно в Z.ai, DeepSeek, Huggingface. ")
        println()
    }
    
    /**
     * Выводит сообщение о завершении работы
     */
    fun printGoodbye() {
        println("Спасибо за использование приложения!")
    }
}
