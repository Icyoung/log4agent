import Foundation

public enum Log4AgentRedactor {
    public static let redacted = "[REDACTED]"

    public static let defaultSensitiveKeys: Set<String> = [
        "authorization",
        "auth",
        "bearer",
        "token",
        "access_token",
        "refresh_token",
        "password",
        "passwd",
        "secret",
        "cookie",
        "set-cookie",
        "session",
        "code"
    ]

    public static func redactAttributes(
        _ attributes: [String: String],
        keys: Set<String> = defaultSensitiveKeys
    ) -> [String: String] {
        attributes.mapValues { value in redactURL(value, keys: keys) }
            .mapKeys { key, value in isSensitiveKey(key, keys: keys) ? redacted : value }
    }

    public static func redactURL(_ value: String, keys: Set<String> = defaultSensitiveKeys) -> String {
        guard value.contains("?") || value.contains("&") else { return value }
        var redactedValue = value
        for key in keys {
            let pattern = "([?&]\(NSRegularExpression.escapedPattern(for: key))=)[^&#\\s]*"
            if let regex = try? NSRegularExpression(pattern: pattern, options: [.caseInsensitive]) {
                let range = NSRange(redactedValue.startIndex..<redactedValue.endIndex, in: redactedValue)
                redactedValue = regex.stringByReplacingMatches(
                    in: redactedValue,
                    options: [],
                    range: range,
                    withTemplate: "$1\(redacted)"
                )
            }
        }
        return redactedValue
    }

    private static func isSensitiveKey(_ key: String, keys: Set<String>) -> Bool {
        let normalized = key.lowercased()
        return keys.contains { sensitive in
            let candidate = sensitive.lowercased()
            return normalized == candidate || normalized.contains(candidate)
        }
    }
}

private extension Dictionary where Key == String, Value == String {
    func mapKeys(_ transform: (String, String) -> String) -> [String: String] {
        Dictionary(uniqueKeysWithValues: map { key, value in (key, transform(key, value)) })
    }
}

