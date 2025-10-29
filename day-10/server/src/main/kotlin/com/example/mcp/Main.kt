package com.example.mcp

import kotlinx.coroutines.runBlocking

fun main(args: Array<String>): Unit = runBlocking {
    val port = args.getOrNull(1)?.toIntOrNull() ?: 3001
    runSseMcpServerUsingKtor(port)
}