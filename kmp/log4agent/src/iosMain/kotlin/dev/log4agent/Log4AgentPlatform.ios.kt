package dev.log4agent

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

internal actual fun defaultLog4AgentEngineFactory(): HttpClientEngineFactory<*> = Darwin

internal actual fun HttpClientConfig<*>.applyLog4AgentPlatformConfig() {
    // iOS Simulator can reach the development host through loopback.
}

internal actual fun defaultLog4AgentEndpoint(): String =
    Log4AgentEndpoint.fromHost(host = "127.0.0.1")

internal actual fun log4AgentPlatformName(): String = "ios"

internal actual fun reportLog4AgentFailure(message: String) {
    println("Log4Agent failed to post log: $message")
}

internal actual fun reportLog4AgentQueued(category: String, endpoint: String) {
    println("Log4Agent queue category=$category endpoint=$endpoint")
}

internal actual fun reportLog4AgentResult(category: String, statusCode: Int) {
    println("Log4Agent posted category=$category status=$statusCode")
}

