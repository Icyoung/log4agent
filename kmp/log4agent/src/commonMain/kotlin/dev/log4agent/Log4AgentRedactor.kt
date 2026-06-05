package dev.log4agent

object Log4AgentRedactor {
    const val REDACTED: String = "[REDACTED]"

    val defaultSensitiveKeys: Set<String> = setOf(
        "authorization",
        "auth",
        "bearer",
        "token",
        "access_token",
        "refresh_token",
        "password",
        "passwd",
        "secret",
        "cookie",
        "set-cookie",
        "session",
        "code",
    )

    fun redactAttributes(
        attributes: Map<String, String>,
        sensitiveKeys: Set<String> = defaultSensitiveKeys,
    ): Map<String, String> = attributes.mapValues { (key, value) ->
        if (isSensitiveKey(key, sensitiveKeys)) REDACTED else redactUrl(value, sensitiveKeys)
    }

    fun redactMessage(message: String, sensitiveKeys: Set<String> = defaultSensitiveKeys): String =
        redactUrl(message, sensitiveKeys)

    fun redactUrl(value: String, sensitiveKeys: Set<String> = defaultSensitiveKeys): String {
        if (!value.contains("?") && !value.contains("&")) return value
        var redacted = value
        sensitiveKeys.forEach { key ->
            val pattern = Regex("(?i)([?&]${Regex.escape(key)}=)[^&#\\s]*")
            redacted = redacted.replace(pattern) { match -> match.groupValues[1] + REDACTED }
        }
        return redacted
    }

    private fun isSensitiveKey(key: String, sensitiveKeys: Set<String>): Boolean {
        val normalized = key.lowercase()
        return sensitiveKeys.any { sensitive ->
            val candidate = sensitive.lowercase()
            normalized == candidate || normalized.contains(candidate)
        }
    }
}

