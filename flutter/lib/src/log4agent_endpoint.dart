class Log4AgentEndpoint {
  static const defaultAndroidEmulator = 'http://10.0.2.2:3100/logs';
  static const defaultIosSimulator = 'http://127.0.0.1:3100/logs';

  static String fromHost(
    String host, {
    int port = 3100,
    String path = '/logs',
    String scheme = 'http',
  }) {
    final normalizedPath = path.startsWith('/') ? path : '/$path';
    return '$scheme://$host:$port$normalizedPath';
  }
}

