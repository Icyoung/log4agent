package dev.log4agent

import android.util.Log
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun defaultLog4AgentEngineFactory(): HttpClientEngineFactory<*> = OkHttp

internal actual fun HttpClientConfig<*>.applyLog4AgentPlatformConfig() {
    // Android Emulator reaches the development host loopback through 10.0.2.2.
}

internal actual fun defaultLog4AgentEndpoint(): String =
    Log4AgentEndpoint.fromHost(host = "10.0.2.2")

internal actual fun log4AgentPlatformName(): String = "android"

internal actual fun reportLog4AgentFailure(message: String) {
    Log.w("Log4Agent", "Failed to post log: $message")
}

internal actual fun reportLog4AgentQueued(category: String, endpoint: String) {
    Log.d("Log4Agent", "Queue log category=$category endpoint=$endpoint")
}

internal actual fun reportLog4AgentResult(category: String, statusCode: Int) {
    Log.d("Log4Agent", "Posted log category=$category status=$statusCode")
}

