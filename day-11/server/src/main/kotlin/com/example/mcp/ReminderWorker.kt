package com.example.mcp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.coroutineContext

class ReminderWorker(
    private val notificationService: NotificationService = ConsoleNotificationService(),
    private val pollIntervalMs: Long = 30_000,
) {

    suspend fun start() {
        CoroutineScope(coroutineContext).launch {
            launch {
                while (isActive) {
                    try {
                        tick()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    delay(pollIntervalMs)
                }
            }
        }
    }

    private suspend fun tick() {
        val nowIso =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC))
        Database.getConnection().use { connection ->
            connection.prepareStatement(
                """
                SELECT n.id as notif_id, t.id as task_id, t.content, t.trigger_time
                FROM notifications n
                JOIN tasks t ON t.id = n.task_id
                WHERE n.sent_at IS NULL AND n.scheduled_at <= ?
                ORDER BY n.id
                LIMIT 100
                """.trimIndent()
            ).use { ps ->
                ps.setString(1, nowIso)
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        val notifId = rs.getInt("notif_id")
                        val task = Task(
                            id = rs.getInt("task_id"),
                            content = rs.getString("content"),
                            triggerTime = rs.getString("trigger_time"),
                        )

                        notificationService.send(task)

                        val sentIso = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
                            ZonedDateTime.now(ZoneOffset.UTC)
                        )
                        connection.prepareStatement(
                            "DELETE FROM notifications WHERE id = ?"
                        ).use { ups ->
                            ups.setInt(1, notifId)
                            ups.executeUpdate()
                        }
                    }
                }
            }
        }
    }
}
