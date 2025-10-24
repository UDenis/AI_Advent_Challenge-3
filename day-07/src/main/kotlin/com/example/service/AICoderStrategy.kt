package com.example.service

import ai.koog.agents.core.agent.entity.AIAgentGraphStrategy
import ai.koog.agents.core.dsl.builder.AIAgentNodeDelegate
import ai.koog.agents.core.dsl.builder.AIAgentSubgraphBuilderBase
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.replaceHistoryWithTLDR
import ai.koog.prompt.dsl.Prompt

internal fun createCoderStrategy(): AIAgentGraphStrategy<String, AICodeResult> {
    return strategy("coder") {
        val requestToWriteCode by nodeLLMRequestForWriteCode()
        val requestToWriteUnitTests by nodeLLMRequestForWriteUnitTest()
        val requestToCodeReview by nodeLLMRequestForCodeReview()

        edge(nodeStart forwardTo requestToWriteCode)
        edge(requestToWriteCode forwardTo requestToWriteUnitTests)
        edge(requestToWriteUnitTests forwardTo requestToCodeReview)
        edge(requestToCodeReview forwardTo nodeFinish)
    }
}

private fun AIAgentSubgraphBuilderBase<*, *>.nodeLLMRequestForWriteCode(): AIAgentNodeDelegate<String, AICodeResult> {
    return node<String, AICodeResult>(
        name = "Программист"
    ) { userPromt ->
        llm.writeSession {
            changeLLMParams(
                prompt.params.copy(
                    temperature = 0.1
                )
            )
            updatePrompt {
                system(
                    """
                    |Роль: Ты — высококвалифицированный инженер-программист, специализирующийся на языке Kotlin. Твоя единственная задача — писать чистый, эффективный и корректный код.
                    |Цель: Анализировать полученные задачи и возвращать исключительно валидный программный код на Kotlin без каких-либо пояснений, комментариев (кроме тех, что в самом коде) или сопроводительного текста.
                    
                    |Строгие ограничения и правила вывода:
                    |- ЗАПРЕЩЕНО добавлять любой текст до или после блока с кодом.
                    |- ЗАПРЕЩЕНО использовать разметку типа ```kotlin. Ты должен возвращать голый код, готовый для компиляции.
                    |- ЗАПРЕЩЕНО писать пояснения, даже если задача сложная.
                    |- ЗАПРЕЩЕНО возвращать сообщения об ошибках или непонимании в текстовой форме. Если задача нечеткая, прими наиболее разумное решение и реализуй его в коде.
                    |- ЕДИНСТВЕННЫЙ ВЫВОД — это готовый к выполнению код на Kotlin.
                """.trimIndent()
                )
                user(userPromt)
            }
            val response = requestLLM()
            AICodeResult(
                code = response.content
            )
        }
    }
}

private fun AIAgentSubgraphBuilderBase<*, *>.nodeLLMRequestForWriteUnitTest(): AIAgentNodeDelegate<AICodeResult, AICodeResult> {
    return node<AICodeResult, AICodeResult>(
        name = "QA"
    ) { aiCodeResult ->
        llm.writeSession {
            changeLLMParams(
                prompt.params.copy(
                    temperature = 0.1
                )
            )
            rewritePrompt {
                //чистим историю
                it.withMessages { emptyList() }
            }
            updatePrompt {
                system(
                    """
                    |Роль: Ты — высококвалифицированный инженер-программист, специализирующийся на языке Kotlin. Твоя единственная задача — писать Unit тесты к предложенному коду.
                    |Цель: Анализировать полученные код и возвращать исключительно валидный программный код Unit тестов на Kotlin без каких-либо пояснений, комментариев (кроме тех, что в самом коде) или сопроводительного текста.
                    
                    |Строгие ограничения и правила вывода:
                    |- ЗАПРЕЩЕНО добавлять любой текст до или после блока с кодом.
                    |- ЗАПРЕЩЕНО использовать разметку типа ```kotlin. Ты должен возвращать голый код, готовый для компиляции.
                    |- ЗАПРЕЩЕНО писать пояснения, даже если задача сложная.
                    |- ЗАПРЕЩЕНО возвращать сообщения об ошибках или непонимании в текстовой форме. Если задача нечеткая, прими наиболее разумное решение и реализуй его в коде.
                    |- ЕДИНСТВЕННЫЙ ВЫВОД — это готовый к выполнению код на Kotlin.
                """.trimIndent()
                )
                user(aiCodeResult.code)
            }
            val response = requestLLM()
            aiCodeResult.copy(
                unitTests = response.content
            )
        }
    }
}

private fun AIAgentSubgraphBuilderBase<*, *>.nodeLLMRequestForCodeReview(): AIAgentNodeDelegate<AICodeResult, AICodeResult> {
    return node<AICodeResult, AICodeResult>(
        name = "Ревьюер"
    ) { aiCodeResult ->
        llm.writeSession {
            changeLLMParams(
                prompt.params.copy(
                    temperature = 0.7
                )
            )
            rewritePrompt {
                //чистим историю
                it.withMessages { emptyList() }
            }
            updatePrompt {
                system(
                    """
                    |Роль: Ты — высококвалифицированный инженер-программист, специализирующийся на языке Kotlin. Твоя единственная задача — делать ревью полученного кода.
                    |Цель: Анализировать полученные код и возвращать список замечания. Укажи недостатки в коде и верни краткий ёмкий ответ.
                """.trimIndent()
                )
                user(
                    """
                    ${aiCodeResult.code}
                    
                    ${aiCodeResult.unitTests}
                """.trimIndent()
                )
            }
            val response = requestLLM()
            aiCodeResult.copy(
                review = response.content
            )
        }
    }
}

data class AICodeResult(
    val code: String,
    val unitTests: String = "",
    val review: String = "",
)