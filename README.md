# Log4Agent

[中文文档](README.zh-CN.md)

Log4Agent is a local logging toolkit for mobile debugging and coding agents. Mobile SDKs send structured logs to a local server on the developer machine; the server stores JSONL logs by device and session so agents can inspect Android, iOS, Flutter, and KMP runtime logs directly from the terminal.

## Why Log4Agent

Mobile debugging often blocks coding agents at the same point: they cannot reliably see device logs.

- Android emulator, iOS simulator, and real devices expose logs through different tools.
- `adb logcat`, Xcode console, and Flutter output are noisy and hard to filter consistently.
- Debugging one reproduced action is easier when logs are scoped by device and run session.
- Multi-platform apps use different logging and HTTP stacks across KMP, Android native, iOS native, and Flutter.
- Agents need a simple workflow: start a local server, let the app post logs, inspect logs with `tail` or `curl`.

Log4Agent provides that local observation surface. It does not replace production logging, crash reporting, or APM; it is designed for local development, simulators, real-device debugging, and agent workflows.

## Contents

- [Defaults](#defaults)
- [Development](#development)
- [Server](#server)
- [KMP Client](#kmp-client)
- [Android Native Client](#android-native-client)
- [iOS Native Client](#ios-native-client)
- [Flutter Client](#flutter-client)
- [Coding Agent Skill](#coding-agent-skill)
- [Inspect Logs](#inspect-logs)

It has these components:

- `log4agent-server`: a Node.js server that runs on the developer machine and stores logs as JSONL.
- `log4agent-kmp`: a Kotlin Multiplatform client.
- `log4agent-android`: an Android native client.
- `Log4Agent` Swift Package: an iOS native client.
- `log4agent` Flutter package: a Flutter client.

## Defaults

- Android emulator: `http://10.0.2.2:3100/logs`
- iOS simulator: `http://127.0.0.1:3100/logs`

Callers can override the full endpoint or configure host/port.

## Development

Run the server in foreground. Incoming logs are printed to stdout and written to JSONL:

```bash
cd server
npm start
```

Publish the KMP client locally:

```bash
cd kmp
./gradlew publishToMavenLocal
```

## Server

Install from npm:

```bash
npm install -g log4agent-server
log4agent-server start --port 3100
log4agent-server start --background --port 3100
log4agent-server status
log4agent-server stop
```

Foreground:

```bash
cd server
npm run start:fg -- --port 3100
```

Background:

```bash
cd server
npm run start:bg -- --port 3100
npm run status
npm run stop
```

Environment variables:

- `LOG4AGENT_PORT`: server port, default `3100`
- `LOG4AGENT_DIR`: JSONL output directory, default `.log4agent` under the terminal working directory
- `LOG4AGENT_REDACT`: set to `false` to disable server-side redaction
- `LOG4AGENT_PID_FILE`: optional background pid file
- `LOG4AGENT_OUT_FILE`: optional background stdout/stderr file

Endpoints:

- `GET /health`
- `POST /sessions`
- `POST /logs`
- `GET /logs?tail=100`
- `GET /logs?deviceId=<device>&sessionId=<session>&tail=100`

Storage layout:

```text
.log4agent/
  2026-06-05/
    pixel_8/
      1749100000000-abcd.session.jsonl
      1749100000000-abcd.jsonl
```

Clients generate a session id when `Log4Agent.configure(...)` runs and perform one `/sessions`
handshake for that initialization. Every log line carries `app`, `deviceId`, and `sessionId`, so the
server can route logs by device and session even if the first log arrives before the handshake record.

Client SDK `configure(...)` calls are idempotent. Repeated calls in the same process keep the existing
session and do not send another `/sessions` handshake. Pass `force = true` when a caller intentionally
wants to replace the config and start a new session.

## KMP Client

Use GitHub Packages:

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/OWNER/Log4Agent")
        credentials {
            username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
            password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
        }
    }
    google()
    mavenCentral()
}

commonMain.dependencies {
    implementation("dev.log4agent:log4agent:0.1.0-SNAPSHOT")
}
```

Or add the local Maven dependency after `publishToMavenLocal`:

```kotlin
repositories {
    mavenLocal()
    google()
    mavenCentral()
}

commonMain.dependencies {
    implementation("dev.log4agent:log4agent:0.1.0-SNAPSHOT")
}
```

Use defaults:

```kotlin
Log4Agent.configure(enabled = true)
Log4Agent.info(
    category = "app.start",
    message = "App started",
    attributes = mapOf("buildType" to "debug"),
)
```

Override the endpoint:

```kotlin
Log4Agent.configure(
    Log4AgentConfig.host(host = "192.168.1.10", port = 3100),
)
```

Use an existing Ktor client:

```kotlin
Log4Agent.configure(
    config = Log4AgentConfig(),
    httpClient = appHttpClient,
)
```

Disable client-side redaction:

```kotlin
Log4Agent.configure(
    Log4AgentConfig(redactionEnabled = false),
)
```

Redaction is enabled by default on both client and server. Built-in rules cover common keys such as
`authorization`, `token`, `password`, `secret`, `cookie`, `session`, and `code`, including URL query
parameters with those names.

## Android Native Client

Use GitHub Packages:

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/OWNER/Log4Agent")
        credentials {
            username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
            password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
        }
    }
    google()
    mavenCentral()
}

dependencies {
    implementation("dev.log4agent:log4agent-android:0.1.0-SNAPSHOT")
}
```

```kotlin
Log4Agent.configure(
    Log4AgentConfig.host("10.0.2.2"),
    okHttpClient = appOkHttpClient,
)

Log4Agent.info("app.start", "Native Android app started")
```

Default endpoint: `http://10.0.2.2:3100/logs`.

## iOS Native Client

Use Swift Package Manager:

```swift
.package(url: "https://github.com/OWNER/Log4Agent.git", from: "0.1.0")
```

```swift
Log4Agent.shared.configure(.init(), session: URLSession.shared)
Log4Agent.shared.info("app.start", "Native iOS app started")
```

Default endpoint: `http://127.0.0.1:3100/logs`.

## Flutter Client

Use a git dependency:

```yaml
dependencies:
  log4agent:
    git:
      url: https://github.com/OWNER/Log4Agent.git
      path: flutter
      ref: main
```

```dart
Log4Agent.configure(
  config: const Log4AgentConfig(),
  client: appHttpClient, // package:http Client, optional
);

Log4Agent.info('app.start', 'Flutter app started');
```

Use Dio:

```dart
Log4Agent.configure(
  config: const Log4AgentConfig(),
  dio: appDio,
);
```

Use a custom transport:

```dart
Log4Agent.configure(
  transport: MyLog4AgentTransport(),
);
```

Flutter defaults to the Android emulator endpoint. Use `Log4AgentEndpoint.defaultIosSimulator`
or `Log4AgentConfig.host(...)` when running on iOS simulator or a real device.

## Coding Agent Skill

This repo includes a standard skill at `skills/log4agent`.

Install it from GitHub:

```bash
python3 ~/.codex/skills/.system/skill-installer/scripts/install-skill-from-github.py --url https://github.com/Icyoung/log4agent/tree/main/skills/log4agent
```

Or install it into an agent skill directory by copying or symlinking that folder, then invoke it as
`$log4agent`. The skill explains how agents should start the local server, integrate SDKs, and inspect
`.log4agent/YYYY-MM-DD/<deviceId>/<sessionId>.jsonl` logs.

Codex skill path example:

```bash
ln -s /path/to/Log4Agent/skills/log4agent ~/.codex/skills/log4agent
```

Claude skill path example:

```bash
ln -s /path/to/Log4Agent/skills/log4agent ~/.agents/skills/log4agent
```

## Inspect Logs

The default log directory is `.log4agent` under the directory where `log4agent-server` was started.

Check server status:

```bash
log4agent-server status
curl -sS http://127.0.0.1:3100/health
```

Find sessions:

```bash
find .log4agent -name '*.session.jsonl' -print
```

Find log files:

```bash
find .log4agent -name '*.jsonl' ! -name '*.session.jsonl' -print
```

Tail the latest logs for one session:

```bash
tail -100 .log4agent/YYYY-MM-DD/<deviceId>/<sessionId>.jsonl
```

Query through the server:

```bash
curl -sS 'http://127.0.0.1:3100/logs?tail=100'
curl -sS 'http://127.0.0.1:3100/logs?deviceId=<deviceId>&sessionId=<sessionId>&tail=100'
```

When debugging a specific user action, first find the latest `.session.jsonl`, then tail the matching `<sessionId>.jsonl` in the same directory.
