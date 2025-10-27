package com.example.output

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import com.example.model.ApiResult
import com.example.service.LlmComparisonResult
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.Panel


/**
 * Сервис для форматированного вывода результатов сравнения LLM API
 */
object OutputFormatter {

    val t = Terminal()
    var tokenizer: HuggingFaceTokenizer =
        HuggingFaceTokenizer.newInstance("deepseek-ai/DeepSeek-V3.2-Exp")

    fun tokenCount(promt:String){
        val tokensCount = tokenizer.encode(promt).ids.size
        println("Token count $tokensCount")
    }

    fun printWelcome() {
        t.println(
            Panel(
                """
                    Изучаем влияние длинны контекста на работу модели.
                    deepseek-ai/DeepSeek-V3.2-Exp:
                        context length: 128K
                        max output:     4K-8K
                        
                    Отправляем по два промта модели и задаём вопросы...
                """.trimIndent()
            )
        )
    }

    fun printСonclusion(str:String) {
        t.println(
            Panel(
                content = str,
                title = "Вывод",
            )
        )
    }

    fun printConditions(str:String) {
        t.println(
            Panel(
                content = str,
            )
        )
    }

    fun printRequest(promt: String) {
        val tokensCount = tokenizer.encode(promt).ids.size
        t.println(TextColors.brightBlue("Запрос к LLM (strLen=${promt.length}, token=$tokensCount):"))
        t.println(TextColors.brightBlue("     - ${promt.take(100)}..."))
    }

    /**
     * Выводит результаты сравнения в заданном формате
     */
    fun printResults(result: LlmComparisonResult) {
        when (val apiResult = result.result) {
            is ApiResult.Error -> TODO()
            is ApiResult.Success -> {
                println(
                    "Ответ: (${result.executionTimeMs}ms," +
                            " totalTokens:${apiResult.content.usage.totalTokens}," +
                            " promptTokens:${apiResult.content.usage.promptTokens}," +
                            " completionTokens:${apiResult.content.usage.completionTokens}):"
                )
                println("     - ${apiResult.content.choices[0].message.content}")
                println()
            }
        }
    }

    fun finish(){
        tokenizer.close();
    }
}
