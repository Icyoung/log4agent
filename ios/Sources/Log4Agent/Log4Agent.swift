import Foundation

public final class Log4Agent {
    public static let shared = Log4Agent()

    private var config = Log4AgentConfig()
    private var session: URLSession = .shared
    private let encoder = JSONEncoder()
    private var sessionId = UUID().uuidString
    private var sessionStarted = false
    private var initialized = false

    public init() {}

    public func configure(_ config: Log4AgentConfig = Log4AgentConfig(), session: URLSession = .shared, force: Bool = false) {
        if initialized && !force { return }
        initialized = true
        self.config = config
        self.session = session
        self.sessionId = UUID().uuidString
        self.sessionStarted = false
        startSession()
    }

    public func debug(_ category: String, _ message: String, attributes: [String: String] = [:]) {
        log(category, message, level: .debug, attributes: attributes)
    }

    public func info(_ category: String, _ message: String, attributes: [String: String] = [:]) {
        log(category, message, level: .info, attributes: attributes)
    }

    public func warn(_ category: String, _ message: String, attributes: [String: String] = [:]) {
        log(category, message, level: .warn, attributes: attributes)
    }

    public func error(_ category: String, _ message: String, attributes: [String: String] = [:]) {
        log(category, message, level: .error, attributes: attributes)
    }

    public func log(
        _ category: String,
        _ message: String,
        level: Log4AgentLevel = .info,
        attributes: [String: String] = [:]
    ) {
        let current = config
        guard current.enabled else { return }
        startSession()
        let safeMessage = current.redactionEnabled
            ? Log4AgentRedactor.redactURL(message, keys: current.redactionKeys)
            : message
        let safeAttributes = current.redactionEnabled
            ? Log4AgentRedactor.redactAttributes(attributes, keys: current.redactionKeys)
            : attributes
        let payload = Log4AgentPayload(
            timestamp: ISO8601DateFormatter().string(from: Date()),
            app: current.app,
            deviceId: current.deviceId,
            sessionId: sessionId,
            platform: "ios",
            level: level.rawValue,
            category: category,
            message: safeMessage,
            attributes: safeAttributes
        )
        guard let data = try? encoder.encode(payload) else { return }
        var request = URLRequest(url: current.endpoint)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "content-type")
        request.httpBody = data
        session.dataTask(with: request).resume()
    }

    private func startSession() {
        let current = config
        guard current.enabled, !sessionStarted else { return }
        sessionStarted = true
        let payload = Log4AgentSessionPayload(
            timestamp: ISO8601DateFormatter().string(from: Date()),
            app: current.app,
            deviceId: current.deviceId,
            sessionId: sessionId,
            platform: "ios"
        )
        guard let data = try? encoder.encode(payload) else { return }
        var request = URLRequest(url: sessionEndpoint(current.endpoint))
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "content-type")
        request.httpBody = data
        session.dataTask(with: request).resume()
    }

    private func sessionEndpoint(_ logEndpoint: URL) -> URL {
        let absolute = logEndpoint.absoluteString
        if absolute.hasSuffix("/logs") {
            return URL(string: String(absolute.dropLast("/logs".count)) + "/sessions")!
        }
        return URL(string: absolute.trimmingCharacters(in: CharacterSet(charactersIn: "/")) + "/sessions")!
    }
}

private struct Log4AgentPayload: Encodable {
    let timestamp: String
    let app: String
    let deviceId: String
    let sessionId: String
    let platform: String
    let level: String
    let category: String
    let message: String
    let attributes: [String: String]
}

private struct Log4AgentSessionPayload: Encodable {
    let timestamp: String
    let app: String
    let deviceId: String
    let sessionId: String
    let platform: String
}
