class Log4AgentRedactor {
  static const redacted = '[REDACTED]';

  static const defaultSensitiveKeys = <String>{
    'authorization',
    'auth',
    'bearer',
    'token',
    'access_token',
    'refresh_token',
    'password',
    'passwd',
    'secret',
    'cookie',
    'set-cookie',
    'session',
    'code',
  };

  static Map<String, String> redactAttributes(
    Map<String, String> attributes, {
    Set<String> keys = defaultSensitiveKeys,
  }) {
    return attributes.map((key, value) {
      if (_isSensitiveKey(key, keys)) return MapEntry(key, redacted);
      return MapEntry(key, redactUrl(value, keys: keys));
    });
  }

  static String redactUrl(
    String value, {
    Set<String> keys = defaultSensitiveKeys,
  }) {
    if (!value.contains('?') && !value.contains('&')) return value;
    var redactedValue = value;
    for (final key in keys) {
      final escaped = RegExp.escape(key);
      redactedValue = redactedValue.replaceAllMapped(
        RegExp('([?&]$escaped=)[^&#\\s]*', caseSensitive: false),
        (match) => '${match.group(1)}$redacted',
      );
    }
    return redactedValue;
  }

  static bool _isSensitiveKey(String key, Set<String> keys) {
    final normalized = key.toLowerCase();
    return keys.any((sensitive) {
      final candidate = sensitive.toLowerCase();
      return normalized == candidate || normalized.contains(candidate);
    });
  }
}

