package dev.log4agent

data class Log4AgentConfig(
    val enabled: Boolean = true,
    val endpoint: String = Log4AgentEndpoint.default(),
    val app: String = "log4agent",
    val deviceId: String = "",
    val redactionEnabled: Boolean = true,
    val redactionKeys: Set<String> = Log4AgentRedactor.defaultSensitiveKeys,
    val connectTimeoutMillis: Long = 500,
    val requestTimeoutMillis: Long = 1_000,
    val socketTimeoutMillis: Long = 1_000,
) {
    companion object {
        fun endpoint(
            url: String,
            enabled: Boolean = true,
            redactionEnabled: Boolean = true,
        ): Log4AgentConfig = Log4AgentConfig(
            enabled = enabled,
            endpoint = url,
            redactionEnabled = redactionEnabled,
        )

        fun host(
            host: String,
            port: Int = Log4AgentEndpoint.DEFAULT_PORT,
            path: String = Log4AgentEndpoint.DEFAULT_PATH,
            scheme: String = "http",
            enabled: Boolean = true,
            redactionEnabled: Boolean = true,
        ): Log4AgentConfig = Log4AgentConfig(
            enabled = enabled,
            endpoint = Log4AgentEndpoint.fromHost(host = host, port = port, path = path, scheme = scheme),
            redactionEnabled = redactionEnabled,
        )
    }
}
