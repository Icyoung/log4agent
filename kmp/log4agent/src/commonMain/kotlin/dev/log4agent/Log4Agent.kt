package dev.log4agent

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object Log4Agent {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val json = Json { encodeDefaults = false }

    private var config: Log4AgentConfig = Log4AgentConfig()
    private var client: HttpClient = newClient(config)
    private var ownsClient: Boolean = true
    private var sessionId: String = newSessionId()
    private var sessionStarted: Boolean = false
    private var initialized: Boolean = false

    fun configure(config: Log4AgentConfig, force: Boolean = false) {
        configure(config = config, httpClient = null, force = force)
    }

    fun configure(config: Log4AgentConfig, httpClient: HttpClient?, force: Boolean = false) {
        if (initialized && !force) return
        initialized = true
        this.config = config
        sessionId = newSessionId()
        sessionStarted = false
        if (httpClient != null) {
            closeOwnedClient()
            client = httpClient
            ownsClient = false
            startSession()
            return
        }
        closeOwnedClient()
        client = newClient(config)
        ownsClient = true
        startSession()
    }

    fun configure(
        endpoint: String = Log4AgentEndpoint.default(),
        enabled: Boolean = true,
        redactionEnabled: Boolean = true,
        httpClient: HttpClient? = null,
        force: Boolean = false,
    ) {
        configure(
            config = Log4AgentConfig(
                enabled = enabled,
                endpoint = endpoint,
                redactionEnabled = redactionEnabled,
            ),
            httpClient = httpClient,
            force = force,
        )
    }

    fun setEnabled(enabled: Boolean) {
        config = config.copy(enabled = enabled)
    }

    fun debug(category: String, message: String, attributes: Map<String, String> = emptyMap()) =
        log(category, message, Log4AgentLevel.Debug, attributes)

    fun info(category: String, message: String, attributes: Map<String, String> = emptyMap()) =
        log(category, message, Log4AgentLevel.Info, attributes)

    fun warn(category: String, message: String, attributes: Map<String, String> = emptyMap()) =
        log(category, message, Log4AgentLevel.Warn, attributes)

    fun error(category: String, message: String, attributes: Map<String, String> = emptyMap()) =
        log(category, message, Log4AgentLevel.Error, attributes)

    fun log(
        category: String,
        message: String,
        level: Log4AgentLevel = Log4AgentLevel.Info,
        attributes: Map<String, String> = emptyMap(),
    ) {
        log(Log4AgentEvent(category = category, message = message, level = level, attributes = attributes))
    }

    @OptIn(ExperimentalTime::class)
    fun log(event: Log4AgentEvent) {
        val current = config
        if (!current.enabled || current.endpoint.isBlank()) return
        startSession()

        val message = if (current.redactionEnabled) {
            Log4AgentRedactor.redactMessage(event.message, current.redactionKeys)
        } else {
            event.message
        }
        val attributes = if (current.redactionEnabled) {
            Log4AgentRedactor.redactAttributes(event.attributes, current.redactionKeys)
        } else {
            event.attributes
        }

        val payload = buildJsonObject {
            put("timestamp", Clock.System.now().toString())
            put("app", current.app)
            put("deviceId", current.deviceId.ifBlank { log4AgentPlatformName() })
            put("sessionId", sessionId)
            put("platform", log4AgentPlatformName())
            put("level", event.level.name.lowercase())
            put("category", event.category)
            put("message", message)
            put("attributes", JsonObject(attributes.mapValues { JsonPrimitive(it.value) }))
        }

        reportLog4AgentQueued(event.category, current.endpoint)
        scope.launch {
            runCatching {
                val response = client.post(current.endpoint) {
                    headers {
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }
                    setBody(json.encodeToString(JsonObject.serializer(), payload))
                }
                reportLog4AgentResult(event.category, response.status.value)
            }.onFailure { error ->
                reportLog4AgentFailure(error.message ?: error::class.simpleName ?: "unknown")
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun startSession() {
        val current = config
        if (sessionStarted || !current.enabled || current.endpoint.isBlank()) return
        sessionStarted = true
        val payload = buildJsonObject {
            put("timestamp", Clock.System.now().toString())
            put("app", current.app)
            put("deviceId", current.deviceId.ifBlank { log4AgentPlatformName() })
            put("sessionId", sessionId)
            put("platform", log4AgentPlatformName())
        }
        scope.launch {
            runCatching {
                client.post(sessionEndpoint(current.endpoint)) {
                    headers {
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }
                    setBody(json.encodeToString(JsonObject.serializer(), payload))
                }
            }.onFailure { error ->
                reportLog4AgentFailure(error.message ?: error::class.simpleName ?: "unknown")
            }
        }
    }

    private fun sessionEndpoint(logEndpoint: String): String =
        if (logEndpoint.endsWith("/logs")) logEndpoint.removeSuffix("/logs") + "/sessions"
        else logEndpoint.trimEnd('/') + "/sessions"

    private fun newSessionId(): String =
        "${Clock.System.now().toEpochMilliseconds()}-${Random.nextInt().toUInt().toString(16)}"

    private fun newClient(config: Log4AgentConfig): HttpClient =
        HttpClient(defaultLog4AgentEngineFactory()) {
            expectSuccess = false
            install(HttpTimeout) {
                connectTimeoutMillis = config.connectTimeoutMillis
                requestTimeoutMillis = config.requestTimeoutMillis
                socketTimeoutMillis = config.socketTimeoutMillis
            }
            applyLog4AgentPlatformConfig()
        }

    private fun closeOwnedClient() {
        if (ownsClient) client.close()
    }
}
