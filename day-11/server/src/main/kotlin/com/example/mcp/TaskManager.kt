package com.example.mcp

import java.sql.ResultSet
import kotlinx.serialization.Serializable
import java.util.Date
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class TaskManager {
    suspend fun addTask(task: Task) {
        Database.getConnection().use { connection ->
            connection.autoCommit = false
            try {
                val taskId: Int
                connection.prepareStatement(
                    "INSERT INTO tasks(content, trigger_time) VALUES(?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS
                ).use { ps ->
                    ps.setString(1, task.content)
                    ps.setString(2, task.triggerTime)
                    ps.executeUpdate()
                    ps.generatedKeys.use { rs ->
                        taskId = if (rs.next()) rs.getInt(1) else run {
                            connection.createStatement().executeQuery("SELECT last_insert_rowid()").use { rid ->
                                rid.next()
                                rid.getInt(1)
                            }
                        }
                    }
                }

                val triggerZdt = ZonedDateTime.parse(task.triggerTime).withZoneSameInstant(ZoneOffset.UTC)
                val scheduledAt = triggerZdt.minusMinutes(30)
                val nowUtc = ZonedDateTime.now(ZoneOffset.UTC)
                val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

                if (!scheduledAt.isAfter(nowUtc)) {
                    // Due or past: send immediately and record as sent
                    ConsoleNotificationService().send(
                        Task(id = taskId, content = task.content, triggerTime = task.triggerTime)
                    )
                    connection.prepareStatement(
                        "INSERT INTO notifications(task_id, scheduled_at, sent_at) VALUES(?, ?, ?)"
                    ).use { ps ->
                        ps.setInt(1, taskId)
                        ps.setString(2, formatter.format(scheduledAt))
                        ps.setString(3, formatter.format(nowUtc))
                        ps.executeUpdate()
                    }
                } else {
                    // Future: insert notification to be sent later
                    connection.prepareStatement(
                        "INSERT INTO notifications(task_id, scheduled_at, sent_at) VALUES(?, ?, NULL)"
                    ).use { ps ->
                        ps.setInt(1, taskId)
                        ps.setString(2, formatter.format(scheduledAt))
                        ps.executeUpdate()
                    }
                }

                connection.commit()
            } catch (e: Exception) {
                connection.rollback()
                throw e
            } finally {
                connection.autoCommit = true
            }
        }
    }

    suspend fun getActualTasks(
        from: Date,
        to: Date,
    ): List<Task> {
        Database.getConnection().use { connection ->
            val fromIso = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(from.toInstant().atZone(ZoneOffset.UTC))
            val toIso = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(to.toInstant().atZone(ZoneOffset.UTC))

            connection.prepareStatement(
                "SELECT id, content, trigger_time FROM tasks WHERE trigger_time >= ? AND trigger_time < ? ORDER BY id DESC"
            ).use { ps ->
                ps.setString(1, fromIso)
                ps.setString(2, toIso)
                ps.executeQuery().use { rs ->
                    val results = mutableListOf<Task>()
                    while (rs.next()) {
                        results.add(rs.toTask())
                    }
                    return results
                }
            }
        }
    }
}


@Serializable
data class Task(
    val id: Int = -1,
    val content: String,
    val triggerTime: String,
)

private fun ResultSet.toTask(): Task = Task(
    id = getInt("id"),
    content = getString("content"),
    triggerTime = getString("trigger_time"),
)