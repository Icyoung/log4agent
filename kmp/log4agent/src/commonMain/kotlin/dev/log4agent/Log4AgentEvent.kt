package dev.log4agent

data class Log4AgentEvent(
    val category: String,
    val message: String,
    val level: Log4AgentLevel = Log4AgentLevel.Info,
    val attributes: Map<String, String> = emptyMap(),
)

