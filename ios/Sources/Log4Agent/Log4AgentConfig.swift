import Foundation

public struct Log4AgentConfig {
    public var enabled: Bool
    public var endpoint: URL
    public var app: String
    public var deviceId: String
    public var redactionEnabled: Bool
    public var redactionKeys: Set<String>

    public init(
        enabled: Bool = true,
        endpoint: URL = Log4AgentEndpoint.defaultURL,
        app: String = "log4agent",
        deviceId: String = "ios",
        redactionEnabled: Bool = true,
        redactionKeys: Set<String> = Log4AgentRedactor.defaultSensitiveKeys
    ) {
        self.enabled = enabled
        self.endpoint = endpoint
        self.app = app
        self.deviceId = deviceId
        self.redactionEnabled = redactionEnabled
        self.redactionKeys = redactionKeys
    }

    public static func host(
        _ host: String,
        port: Int = 3100,
        path: String = "/logs",
        scheme: String = "http"
    ) -> Log4AgentConfig {
        Log4AgentConfig(endpoint: Log4AgentEndpoint.fromHost(host, port: port, path: path, scheme: scheme))
    }
}
