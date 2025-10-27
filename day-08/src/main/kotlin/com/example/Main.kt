package com.example

import com.example.config.ApiConfig
import com.example.output.OutputFormatter
import com.example.service.DeepSeekService
import com.example.service.LlmComparisonService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking

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
        val deepSeekApiKey = ApiConfig.getApiKeys()
        val httpClient = ApiConfig.createHttpClient()

        val deepSeekService = DeepSeekService(httpClient, deepSeekApiKey)

        val comparisonService = LlmComparisonService(deepSeekService)

        // Приветственное сообщение
        OutputFormatter.printWelcome()

//        if (TEST_PROMT.isNotEmpty()) {
//            OutputFormatter.tokenCount(TEST_PROMT)
//            OutputFormatter.tokenCount(TEST_PROMT1)
//            OutputFormatter.tokenCount(TEST_PROMT + TEST_PROMT1)
//
//            return@runBlocking
//        }

        //-------------
        OutputFormatter.printConditions("1. Просто примерчик на коротком контексте.")
        comparisonService.systemPromt("Ты ассистент по имени Валли.")
        comparisonService.callAndPrintOutput("Привет. Меня зовут Денис и я разработчик. Расскажи кто такой Пушкин?")
        comparisonService.callAndPrintOutput("А я кто такой?")
        OutputFormatter.printСonclusion("Видим что модель легко и быстро даёт ответ.")
        comparisonService.clearHistory()
        println("-----------------------------------------")


//        OutputFormatter.printConditions(
//            "2. Пример с длинным контекста(124K токенов). Отправляем два длинных промта\nпо 96K токенов и 28K токенов.\n" +
//                    "Первый промт содержит моё имя. Мы скарвливаем его модели\n" +
//                    "Второй промт тоже длинный и мы в конце просим его ответить как меня зовут."
//        )
//
//        comparisonService.callAndPrintOutput(TEST_PROMT)
//        comparisonService.callAndPrintOutput(TEST_PROMT1)
//        OutputFormatter.printСonclusion(
//            "Моделька ответил. Но это было долго))"
//        )
//        comparisonService.clearHistory()
//        println("-----------------------------------------")


        OutputFormatter.printConditions(
            "3. Пример переполнения контекста. Отправляем два длинных промта\nпо 115K токенов и 38K токенов.\n" +
                    "Первый промт содержит моё имя. Мы скарвливаем его модели\n" +
                    "Второй промт тоже длинный и мы в конце мы просим его ответить как меня зовут."
        )

        comparisonService.callAndPrintOutput(LONG_PROMT1)
        comparisonService.callAndPrintOutput(LONG_PROMT2)
        OutputFormatter.printСonclusion(
            "Моделька не шмогла!. Ответила ошибкой\n" +
                    "Тут нужно сделать либо обрезку (truncation) истории,\n" +
                    "либо сжатие (summarization)."
        )
        comparisonService.clearHistory()
        println("-----------------------------------------")

        OutputFormatter.printConditions(
            "4. Пример c этими же промтами. Тут мы считаем токены\n" +
                    "и делам сжатие при привышении длинны контекста\n." +
                    "Для подчёта токенов используем HuggingFaceTokenizer."
        )
        comparisonService.callAndPrintOutput(LONG_PROMT1)
        comparisonService.callAndPrintOutput(LONG_PROMT2, true)
        OutputFormatter.printСonclusion("Модель смогла ответить")
        comparisonService.clearHistory()
        println("-----------------------------------------")
    } finally {
        OutputFormatter.finish()
    }
}

suspend fun LlmComparisonService.callAndPrintOutput(
    promt: String,
    summariseLongContext: Boolean = false,
) {
    try {
        OutputFormatter.printRequest(promt)
        val results = sendMessage(promt, summariseLongContext)
        OutputFormatter.printResults(results)
    } catch (ignore: CancellationException) {
        throw ignore
    } catch (e: IllegalStateException) {
        // Ошибка конфигурации (отсутствующие API ключи)
        println("Ошибка конфигурации: ${e.message}")
        println("Убедитесь, что установлены переменные окружения ZAI_API_KEY и DEEPSEEK_API_KEY")
    } catch (e: Exception) {
        // Общие ошибки
        println("Произошла неожиданная ошибка: ${e.message}")
    }
}


