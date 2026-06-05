package dev.log4agent

object Log4AgentRedactor {
    const val REDACTED = "[REDACTED]"

    val defaultSensitiveKeys = setOf(
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

    fun redactAttributes(attributes: Map<String, String>, keys: Set<String> = defaultSensitiveKeys): Map<String, String> =
        attributes.mapValues { (key, value) ->
            if (isSensitiveKey(key, keys)) REDACTED else redactUrl(value, keys)
        }

    fun redactUrl(value: String, keys: Set<String> = defaultSensitiveKeys): String {
        if (!value.contains("?") && !value.contains("&")) return value
        var redacted = value
        keys.forEach { key ->
            val pattern = Regex("(?i)([?&]${Regex.escape(key)}=)[^&#\\s]*")
            redacted = redacted.replace(pattern) { match -> match.groupValues[1] + REDACTED }
        }
        return redacted
    }

    private fun isSensitiveKey(key: String, keys: Set<String>): Boolean {
        val normalized = key.lowercase()
        return keys.any { normalized == it.lowercase() || normalized.contains(it.lowercase()) }
    }
}

