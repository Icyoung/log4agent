import 'log4agent_endpoint.dart';
import 'log4agent_redactor.dart';

class Log4AgentConfig {
  const Log4AgentConfig({
    this.enabled = true,
    this.endpoint = Log4AgentEndpoint.defaultAndroidEmulator,
    this.app = 'log4agent',
    this.deviceId = 'flutter',
    this.redactionEnabled = true,
    this.redactionKeys = Log4AgentRedactor.defaultSensitiveKeys,
  });

  final bool enabled;
  final String endpoint;
  final String app;
  final String deviceId;
  final bool redactionEnabled;
  final Set<String> redactionKeys;

  factory Log4AgentConfig.endpoint(
    String url, {
    bool enabled = true,
    bool redactionEnabled = true,
  }) {
    return Log4AgentConfig(
      enabled: enabled,
      endpoint: url,
      redactionEnabled: redactionEnabled,
    );
  }

  factory Log4AgentConfig.host(
    String host, {
    int port = 3100,
    String path = '/logs',
    String scheme = 'http',
    bool enabled = true,
    bool redactionEnabled = true,
  }) {
    return Log4AgentConfig(
      enabled: enabled,
      endpoint: Log4AgentEndpoint.fromHost(host, port: port, path: path, scheme: scheme),
      redactionEnabled: redactionEnabled,
    );
  }
}
