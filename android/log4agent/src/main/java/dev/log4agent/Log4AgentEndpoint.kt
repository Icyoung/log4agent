package dev.log4agent

object Log4AgentEndpoint {
    fun default(): String = fromHost("10.0.2.2")

    fun fromHost(host: String, port: Int = 3100, path: String = "/logs", scheme: String = "http"): String {
        val normalizedPath = if (path.startsWith("/")) path else "/$path"
        return "$scheme://$host:$port$normalizedPath"
    }
}

