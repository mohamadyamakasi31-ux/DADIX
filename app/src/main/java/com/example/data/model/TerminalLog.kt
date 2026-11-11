package com.example.data.model

enum class LogLevel {
    COMMAND,
    OUTPUT,
    SUCCESS,
    ERROR,
    INFO
}

data class TerminalLog(
    val level: LogLevel,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
