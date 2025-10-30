package com.example.mcp

interface NotificationService {
    suspend fun send(task: Task)
}

class ConsoleNotificationService : NotificationService {
    override suspend fun send(task: Task) {
        println("[REMINDER] ${task.triggerTime} — ${task.content} (taskId=${task.id}). Send notofication")
    }
}


