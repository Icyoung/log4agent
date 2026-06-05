package dev.log4agent

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.time.Instant
import java.util.UUID

object Log4Agent {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private var config = Log4AgentConfig()
    private var client = OkHttpClient()
    private var sessionId = UUID.randomUUID().toString()
    private var sessionStarted = false

    fun configure(config: Log4AgentConfig = Log4AgentConfig(), okHttpClient: OkHttpClient? = null) {
        this.config = config
        sessionId = UUID.randomUUID().toString()
        sessionStarted = false
        if (okHttpClient != null) client = okHttpClient
        startSession()
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
        val current = config
        if (!current.enabled || current.endpoint.isBlank()) return
        startSession()
        val safeMessage = if (current.redactionEnabled) Log4AgentRedactor.redactUrl(message, current.redactionKeys) else message
        val safeAttributes = if (current.redactionEnabled) Log4AgentRedactor.redactAttributes(attributes, current.redactionKeys) else attributes
        val body = buildJson(category, safeMessage, level, safeAttributes)
        val requestBuilder = Request.Builder()
            .url(current.endpoint)
            .post(body.toRequestBody(jsonMediaType))
            .header("content-type", "application/json")
        client.newCall(requestBuilder.build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = Unit
            override fun onResponse(call: Call, response: Response) {
                response.close()
            }
        })
    }

    private fun startSession() {
        val current = config
        if (sessionStarted || !current.enabled || current.endpoint.isBlank()) return
        sessionStarted = true
        val body = JSONObject()
            .put("timestamp", Instant.now().toString())
            .put("app", current.app)
            .put("deviceId", current.deviceId)
            .put("sessionId", sessionId)
            .put("platform", "android")
            .toString()
        val request = Request.Builder()
            .url(sessionEndpoint(current.endpoint))
            .post(body.toRequestBody(jsonMediaType))
            .header("content-type", "application/json")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = Unit
            override fun onResponse(call: Call, response: Response) {
                response.close()
            }
        })
    }

    private fun sessionEndpoint(logEndpoint: String): String =
        if (logEndpoint.endsWith("/logs")) logEndpoint.removeSuffix("/logs") + "/sessions"
        else logEndpoint.trimEnd('/') + "/sessions"

    private fun buildJson(
        category: String,
        message: String,
        level: Log4AgentLevel,
        attributes: Map<String, String>,
    ): String {
        return JSONObject()
            .put("timestamp", Instant.now().toString())
            .put("app", config.app)
            .put("deviceId", config.deviceId)
            .put("sessionId", sessionId)
            .put("platform", "android")
            .put("level", level.name.lowercase())
            .put("category", category)
            .put("message", message)
            .put("attributes", JSONObject(attributes))
            .toString()
    }
}
