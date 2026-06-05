package dev.log4agent

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineFactory

internal actual fun defaultLog4AgentEngineFactory(): HttpClientEngineFactory<*> =
    error("Log4Agent does not provide a default wasmJs HTTP engine. Pass an HttpClient explicitly.")

internal actual fun HttpClientConfig<*>.applyLog4AgentPlatformConfig() = Unit

internal actual fun defaultLog4AgentEndpoint(): String = ""

internal actual fun log4AgentPlatformName(): String = "wasmJs"

internal actual fun reportLog4AgentFailure(message: String) {
    println("Log4Agent failed to post log: $message")
}

internal actual fun reportLog4AgentQueued(category: String, endpoint: String) {
    println("Log4Agent queue category=$category endpoint=$endpoint")
}

internal actual fun reportLog4AgentResult(category: String, statusCode: Int) {
    println("Log4Agent posted category=$category status=$statusCode")
}
