package dev.log4agent

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineFactory

internal expect fun defaultLog4AgentEngineFactory(): HttpClientEngineFactory<*>

internal expect fun HttpClientConfig<*>.applyLog4AgentPlatformConfig()

internal expect fun defaultLog4AgentEndpoint(): String

internal expect fun log4AgentPlatformName(): String

internal expect fun reportLog4AgentFailure(message: String)

internal expect fun reportLog4AgentQueued(category: String, endpoint: String)

internal expect fun reportLog4AgentResult(category: String, statusCode: Int)

