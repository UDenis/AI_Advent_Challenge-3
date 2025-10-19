package ru.aiadventchallenge.chat

import kotlinx.coroutines.*

/**
 * Типы анимации загрузки
 */
enum class AnimationType {
    SPINNER,  // Классический спиннер
    DOTS,     // Точки
    PULSE,    // Пульсация
    WAVE      // Волна
}

/**
 * Класс для отображения эффекта загрузки в терминале
 */
class LoadingIndicator {
    private var isRunning = false
    private var loadingJob: Job? = null
    
    // Различные наборы анимации
    private val spinnerFrames = listOf("⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏")
    private val dotsFrames = listOf(".", "..", "...", "....", ".", "..", "...", "....",)
    private val pulseFrames = listOf("●", "○", "●", "○", "●", "○")
    private val waveFrames = listOf("▁", "▃", "▄", "▅", "▆", "▇", "█", "▇", "▆", "▅", "▄", "▃")
    
    /**
     * Запускает анимацию загрузки
     * @param message Сообщение, которое отображается во время загрузки
     * @param animationType Тип анимации (spinner, dots, pulse)
     */
    fun startLoading(message: String = "AI думает", animationType: AnimationType = AnimationType.SPINNER) {
        if (isRunning) return
        
        isRunning = true
        loadingJob = CoroutineScope(Dispatchers.IO).launch {
            val frames = when (animationType) {
                AnimationType.SPINNER -> spinnerFrames
                AnimationType.DOTS -> dotsFrames
                AnimationType.PULSE -> pulseFrames
                AnimationType.WAVE -> waveFrames
            }
            var frameIndex = 0
            
            while (isRunning) {
                print("\r$message ${frames[frameIndex]}")
                System.out.flush()
                
                frameIndex = (frameIndex + 1) % frames.size
                delay(100)
            }
        }
    }
    
    /**
     * Останавливает анимацию загрузки
     */
    fun stopLoading() {
        if (!isRunning) return
        
        isRunning = false
        loadingJob?.cancel()
        loadingJob = null
        
        // Очищаем строку загрузки
        print("\r${" ".repeat(50)}\r")
        System.out.flush()
    }
    
    /**
     * Выполняет блок кода с отображением загрузки
     * @param message Сообщение загрузки
     * @param animationType Тип анимации
     * @param block Блок кода для выполнения
     * @return Результат выполнения блока
     */
    suspend fun <T> withLoading(
        message: String = "AI думает", 
        animationType: AnimationType = AnimationType.SPINNER,
        block: suspend () -> T
    ): T {
        startLoading(message, animationType)
        return try {
            block()
        } finally {
            stopLoading()
        }
    }
}
