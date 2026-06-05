package dev.log4agent

object Log4AgentEndpoint {
    const val DEFAULT_PORT: Int = 3100
    const val DEFAULT_PATH: String = "/logs"

    fun default(): String = defaultLog4AgentEndpoint()

    fun fromHost(
        host: String,
        port: Int = DEFAULT_PORT,
        path: String = DEFAULT_PATH,
        scheme: String = "http",
    ): String {
        val normalizedPath = if (path.startsWith("/")) path else "/$path"
        return "$scheme://$host:$port$normalizedPath"
    }
}

