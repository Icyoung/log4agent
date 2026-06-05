import Foundation

public enum Log4AgentEndpoint {
    public static let defaultURL = fromHost("127.0.0.1")

    public static func fromHost(
        _ host: String,
        port: Int = 3100,
        path: String = "/logs",
        scheme: String = "http"
    ) -> URL {
        var components = URLComponents()
        components.scheme = scheme
        components.host = host
        components.port = port
        components.path = path.hasPrefix("/") ? path : "/\(path)"
        return components.url!
    }
}

