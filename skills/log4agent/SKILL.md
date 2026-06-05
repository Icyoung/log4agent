---
name: log4agent
description: Use when a coding agent needs to install, integrate, run, or inspect Log4Agent mobile logs from Android emulator, iOS simulator, KMP, Android native, iOS native, or Flutter apps. Log4Agent is a local server plus client SDKs that let agents view mobile app logs by device and session.
---

# Log4Agent

Log4Agent lets a coding agent collect and inspect mobile app logs from a local machine. The app posts structured JSON logs to a local Node server; the server stores logs by date, device, and session.

## Server Workflow

Install or run the server:

```bash
npm install -g log4agent-server
log4agent-server start --port 3100
```

Foreground mode prints incoming logs to stdout and also writes JSONL files.

Background mode:

```bash
log4agent-server start --background --port 3100
log4agent-server status
log4agent-server stop
```

Default storage is `.log4agent` under the terminal working directory. Use `--dir <path>` only when the user intentionally wants a different log root.

Server endpoints:

- `GET /health`
- `POST /sessions`
- `POST /logs`
- `GET /logs?tail=100`
- `GET /logs?deviceId=<device>&sessionId=<session>&tail=100`

## Client Defaults

- Android emulator endpoint: `http://10.0.2.2:3100/logs`
- iOS simulator endpoint: `http://127.0.0.1:3100/logs`
- Real devices need the developer machine LAN IP, for example `http://192.168.1.10:3100/logs`.

Clients generate a session when configured, call `/sessions` once, then attach `app`, `deviceId`, and `sessionId` to each log. The server stores logs as:

```text
.log4agent/
  YYYY-MM-DD/
    <deviceId>/
      <sessionId>.session.jsonl
      <sessionId>.jsonl
```

## SDK Integration

KMP via GitHub Packages:

```kotlin
commonMain.dependencies {
    implementation("dev.log4agent:log4agent:<version>")
}

Log4Agent.configure(
    Log4AgentConfig(app = "my_app", deviceId = "android-emulator"),
    httpClient = appKtorClient, // optional Ktor HttpClient
)
Log4Agent.info("app.start", "App started")
```

Android native:

```kotlin
dependencies {
    implementation("dev.log4agent:log4agent-android:<version>")
}

Log4Agent.configure(
    Log4AgentConfig(app = "my_android_app", deviceId = "pixel_8"),
    okHttpClient = appOkHttpClient, // optional OkHttpClient
)
Log4Agent.info("app.start", "Native Android app started")
```

iOS native via Swift Package Manager:

```swift
.package(url: "https://github.com/OWNER/Log4Agent.git", from: "<version>")

Log4Agent.shared.configure(
    Log4AgentConfig(app: "my_ios_app", deviceId: "iphone_sim"),
    session: URLSession.shared
)
Log4Agent.shared.info("app.start", "Native iOS app started")
```

Flutter via git dependency:

```yaml
dependencies:
  log4agent:
    git:
      url: https://github.com/OWNER/Log4Agent.git
      path: flutter
      ref: <version>
```

```dart
Log4Agent.configure(
  config: const Log4AgentConfig(app: 'my_flutter_app', deviceId: 'pixel_8'),
  dio: appDio, // optional Dio
  // client: appHttpClient, // optional package:http Client
)
Log4Agent.info('app.start', 'Flutter app started');
```

## Inspect Logs

When asked to view logs, first identify the log root. Default is `.log4agent` in the directory where `log4agent-server` was started.

Find sessions:

```bash
find .log4agent -name '*.session.jsonl' -print
```

List recent log files:

```bash
find .log4agent -name '*.jsonl' ! -name '*.session.jsonl' -print
```

Tail latest logs:

```bash
tail -100 .log4agent/YYYY-MM-DD/<deviceId>/<sessionId>.jsonl
```

Query server if it is running:

```bash
curl -sS 'http://127.0.0.1:3100/logs?tail=100'
curl -sS 'http://127.0.0.1:3100/logs?deviceId=<deviceId>&sessionId=<sessionId>&tail=100'
```

Prefer session-scoped logs when debugging a user action. Use broad `tail=100` only when device or session is unknown.

## Agent Debugging Checklist

1. Confirm server status with `log4agent-server status` or `curl -sS http://127.0.0.1:3100/health`.
2. Confirm the app endpoint matches platform: `10.0.2.2` for Android emulator, `127.0.0.1` for iOS simulator, LAN IP for real devices.
3. Find the newest `.session.jsonl` to identify device and session.
4. Tail the matching `<sessionId>.jsonl`.
5. If logs are missing, check whether `Log4Agent.configure(enabled = true, ...)` ran and whether the app can reach the host.

