# log4agent

[![KMP Maven Central](https://img.shields.io/maven-central/v/io.github.icyoung/log4agent?label=KMP%20Maven%20Central&color=7F52FF)](https://central.sonatype.com/artifact/io.github.icyoung/log4agent)
[![Android Maven Central](https://img.shields.io/maven-central/v/io.github.icyoung/log4agent-android-native?label=Android%20Maven%20Central&color=3DDC84)](https://central.sonatype.com/artifact/io.github.icyoung/log4agent-android-native)
[![Flutter pub.dev](https://img.shields.io/pub/v/log4agent?label=Flutter%20pub.dev&color=02569B)](https://pub.dev/packages/log4agent)
[![npm server](https://img.shields.io/npm/v/log4agent-server?label=npm%20server&color=CB3837)](https://www.npmjs.com/package/log4agent-server)

Flutter client for Log4Agent, a local mobile log bridge that helps coding agents inspect app logs by device and session.

## Packages

- [KMP client: `io.github.icyoung:log4agent`](https://central.sonatype.com/artifact/io.github.icyoung/log4agent)
- [Android native client: `io.github.icyoung:log4agent-android-native`](https://central.sonatype.com/artifact/io.github.icyoung/log4agent-android-native)
- [Flutter client: `log4agent`](https://pub.dev/packages/log4agent)
- [Local server: `log4agent-server`](https://www.npmjs.com/package/log4agent-server)

## Install

```yaml
dependencies:
  log4agent: ^0.1.0
```

## Usage

```dart
import 'package:log4agent/log4agent.dart';

void main() {
  Log4Agent.configure(
    config: const Log4AgentConfig(
      app: 'my_flutter_app',
      deviceId: 'pixel_8',
    ),
  );

  Log4Agent.info('app.start', 'Flutter app started');
}
```

Use an existing `package:http` client:

```dart
Log4Agent.configure(
  config: const Log4AgentConfig(),
  client: appHttpClient,
);
```

Use Dio:

```dart
Log4Agent.configure(
  config: const Log4AgentConfig(),
  dio: appDio,
);
```

## Endpoints

Default endpoint:

```text
http://10.0.2.2:3100/logs
```

Use `Log4AgentEndpoint.defaultIosSimulator` for an iOS simulator, or configure a host for a real device:

```dart
Log4Agent.configure(
  config: Log4AgentConfig.host('192.168.1.10'),
);
```

## Server

Run the local server on your development machine:

```bash
npm install -g log4agent-server
log4agent-server start --background --port 3100
```

Logs are written to `.log4agent` under the directory where the server starts.
