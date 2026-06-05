package dev.log4agent

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun defaultLog4AgentEngineFactory(): HttpClientEngineFactory<*> = OkHttp

internal actual fun HttpClientConfig<*>.applyLog4AgentPlatformConfig() = Unit

internal actual fun defaultLog4AgentEndpoint(): String =
    Log4AgentEndpoint.fromHost(host = "127.0.0.1")

internal actual fun log4AgentPlatformName(): String = "desktop"

internal actual fun reportLog4AgentFailure(message: String) {
    println("Log4Agent failed to post log: $message")
}

internal actual fun reportLog4AgentQueued(category: String, endpoint: String) {
    println("Log4Agent queue category=$category endpoint=$endpoint")
}

internal actual fun reportLog4AgentResult(category: String, statusCode: Int) {
    println("Log4Agent posted category=$category status=$statusCode")
}
