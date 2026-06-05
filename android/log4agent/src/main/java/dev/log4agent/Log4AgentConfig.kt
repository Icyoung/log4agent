package dev.log4agent

data class Log4AgentConfig(
    val enabled: Boolean = true,
    val endpoint: String = Log4AgentEndpoint.default(),
    val app: String = "log4agent",
    val deviceId: String = "android",
    val redactionEnabled: Boolean = true,
    val redactionKeys: Set<String> = Log4AgentRedactor.defaultSensitiveKeys,
) {
    companion object {
        fun endpoint(url: String, enabled: Boolean = true): Log4AgentConfig =
            Log4AgentConfig(enabled = enabled, endpoint = url)

        fun host(host: String, port: Int = 3100, path: String = "/logs"): Log4AgentConfig =
            Log4AgentConfig(endpoint = Log4AgentEndpoint.fromHost(host, port, path))
    }
}
