package com.example.mcp

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

object Database {
    private const val JDBC_URL = "jdbc:sqlite:tasks.db"

    fun init() {
        // Ensure driver is loaded (usually not required with JDBC 4, but harmless)
        try {
            Class.forName("org.sqlite.JDBC")
        } catch (_: ClassNotFoundException) {
            // Ignore, driver is auto-registered in modern environments
        }

        getConnection().use { connection ->
            connection.createStatement().use { statement ->
                statement.execute(
                    """
                    CREATE TABLE IF NOT EXISTS tasks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        content TEXT NOT NULL,
                        trigger_time DATETIME NOT NULL
                    )
                    """.trimIndent()
                )
                statement.execute(
                    """
                    CREATE TABLE IF NOT EXISTS notifications (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        task_id INTEGER NOT NULL,
                        scheduled_at DATETIME NOT NULL,
                        sent_at DATETIME NULL,
                        FOREIGN KEY(task_id) REFERENCES tasks(id)
                    )
                    """.trimIndent()
                )
                statement.execute(
                    """
                    CREATE INDEX IF NOT EXISTS idx_notifications_due
                    ON notifications(sent_at, scheduled_at)
                    """.trimIndent()
                )
            }
        }
    }

    fun getConnection(): Connection {
        return DriverManager.getConnection(JDBC_URL)
    }
}


