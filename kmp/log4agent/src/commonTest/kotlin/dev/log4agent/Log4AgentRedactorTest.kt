package dev.log4agent

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class Log4AgentRedactorTest {
    @Test
    fun redactsSensitiveAttributeKeys() {
        val redacted = Log4AgentRedactor.redactAttributes(
            mapOf(
                "Authorization" to "Bearer abc",
                "symbol" to "BTC-USDT",
            ),
        )

        assertEquals(Log4AgentRedactor.REDACTED, redacted["Authorization"])
        assertEquals("BTC-USDT", redacted["symbol"])
    }

    @Test
    fun redactsSensitiveQueryParams() {
        val redacted = Log4AgentRedactor.redactUrl(
            "https://example.com/login?token=abc&symbol=BTC-USDT&code=123",
        )

        assertFalse(redacted.contains("abc"))
        assertFalse(redacted.contains("123"))
        assertEquals(
            "https://example.com/login?token=[REDACTED]&symbol=BTC-USDT&code=[REDACTED]",
            redacted,
        )
    }
}

