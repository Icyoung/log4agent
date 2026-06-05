# Log4Agent

[English](README.md)

Log4Agent 是一个给移动端调试和 coding agent 使用的本机日志工具。移动端 SDK 把结构化日志发到开发机上的本地 server，server 按设备和 session 存成 JSONL，agent 可以直接在终端查看 Android、iOS、Flutter、KMP 的运行日志。

## 为什么需要它

移动端开发里，coding agent 经常卡在“看不到设备日志”这一步：

- Android 模拟器、iOS 模拟器、真机日志入口不同，agent 很难稳定拿到同一套日志。
- `adb logcat`、Xcode console、Flutter run 输出容易混在一起，筛选成本高。
- 复现一次用户操作时，最好按“设备 + 本次运行 session”查看日志，而不是在海量全局日志里搜索。
- 多端项目里 KMP、Android 原生、iOS 原生、Flutter 往往使用不同 HTTP/日志栈，缺少统一的 agent 观察面。
- agent 需要一个简单协议：启动本机 server，移动端写日志，agent 用 `tail` 或 `curl` 直接查看。

Log4Agent 解决的是本地观察面问题，不替代线上日志、Crash 平台或 APM。它默认面向本地开发、模拟器、真机联调和 agent 调试。

## 目录

- [默认地址](#默认地址)
- [本地开发](#本地开发)
- [Server 安装与运行](#server-安装与运行)
- [KMP 集成](#kmp-集成)
- [Android 原生集成](#android-原生集成)
- [iOS 原生集成](#ios-原生集成)
- [Flutter 集成](#flutter-集成)
- [Coding Agent Skill](#coding-agent-skill)
- [查看日志](#查看日志)

## 组成

- `log4agent-server`: 运行在开发机上的 Node.js server，负责接收日志并写入 JSONL。
- `log4agent-kmp`: Kotlin Multiplatform 客户端。
- `log4agent-android`: Android 原生客户端。
- `Log4Agent` Swift Package: iOS 原生客户端。
- `log4agent` Flutter package: Flutter 客户端。

## 默认地址

- Android emulator: `http://10.0.2.2:3100/logs`
- iOS simulator: `http://127.0.0.1:3100/logs`

真机需要配置开发机局域网 IP，例如 `http://192.168.1.10:3100/logs`。

## 本地开发

前台启动 server。收到的日志会同步打印到 stdout，并写入 JSONL：

```bash
cd server
npm start
```

发布 KMP 客户端到本机 Maven：

```bash
cd kmp
./gradlew publishToMavenLocal
```

## Server 安装与运行

通过 npm 安装：

```bash
npm install -g log4agent-server
log4agent-server start --port 3100
log4agent-server start --background --port 3100
log4agent-server status
log4agent-server stop
```

仓库内前台运行：

```bash
cd server
npm run start:fg -- --port 3100
```

仓库内后台运行：

```bash
cd server
npm run start:bg -- --port 3100
npm run status
npm run stop
```

环境变量：

- `LOG4AGENT_PORT`: server 端口，默认 `3100`
- `LOG4AGENT_DIR`: JSONL 输出目录，默认是运行命令所在目录的 `.log4agent`
- `LOG4AGENT_REDACT`: 设置为 `false` 可关闭 server 侧脱敏
- `LOG4AGENT_PID_FILE`: 可选后台 pid 文件
- `LOG4AGENT_OUT_FILE`: 可选后台 stdout/stderr 文件

接口：

- `GET /health`
- `POST /sessions`
- `POST /logs`
- `GET /logs?tail=100`
- `GET /logs?deviceId=<device>&sessionId=<session>&tail=100`

存储结构：

```text
.log4agent/
  2026-06-05/
    pixel_8/
      1749100000000-abcd.session.jsonl
      1749100000000-abcd.jsonl
```

客户端在 `Log4Agent.configure(...)` 时生成 session id，并对 `/sessions` 做一次握手。每条日志都会带 `app`、`deviceId`、`sessionId`，所以即使第一条日志比握手记录先到，server 也可以按设备和 session 路由日志。

各端 SDK 的 `configure(...)` 都是幂等的。同一进程里重复调用会复用已有 session，不会再次发送 `/sessions` 握手。调用方确实需要替换配置并开启新 session 时，可以传 `force = true`。

## KMP 集成

通过 GitHub Packages：

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

本地 Maven：

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

使用默认配置：

```kotlin
Log4Agent.configure(enabled = true)
Log4Agent.info(
    category = "app.start",
    message = "App started",
    attributes = mapOf("buildType" to "debug"),
)
```

覆盖 endpoint：

```kotlin
Log4Agent.configure(
    Log4AgentConfig.host(host = "192.168.1.10", port = 3100),
)
```

使用已有 Ktor client：

```kotlin
Log4Agent.configure(
    config = Log4AgentConfig(),
    httpClient = appHttpClient,
)
```

关闭客户端脱敏：

```kotlin
Log4Agent.configure(
    Log4AgentConfig(redactionEnabled = false),
)
```

客户端和 server 默认都开启脱敏。内置规则覆盖 `authorization`、`token`、`password`、`secret`、`cookie`、`session`、`code` 等常见 key，也会处理 URL query 参数。

## Android 原生集成

通过 GitHub Packages：

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

默认 endpoint: `http://10.0.2.2:3100/logs`。

## iOS 原生集成

通过 Swift Package Manager：

```swift
.package(url: "https://github.com/OWNER/Log4Agent.git", from: "0.1.0")
```

```swift
Log4Agent.shared.configure(.init(), session: URLSession.shared)
Log4Agent.shared.info("app.start", "Native iOS app started")
```

默认 endpoint: `http://127.0.0.1:3100/logs`。

## Flutter 集成

通过 git dependency：

```yaml
dependencies:
  log4agent:
    git:
      url: https://github.com/OWNER/Log4Agent.git
      path: flutter
      ref: main
```

使用 `package:http`：

```dart
Log4Agent.configure(
  config: const Log4AgentConfig(),
  client: appHttpClient,
);

Log4Agent.info('app.start', 'Flutter app started');
```

使用 Dio：

```dart
Log4Agent.configure(
  config: const Log4AgentConfig(),
  dio: appDio,
);
```

使用自定义 transport：

```dart
Log4Agent.configure(
  transport: MyLog4AgentTransport(),
);
```

Flutter 默认使用 Android emulator endpoint。运行 iOS simulator 或真机时，用 `Log4AgentEndpoint.defaultIosSimulator` 或 `Log4AgentConfig.host(...)` 配置。

## Coding Agent Skill

仓库内置标准 skill：`skills/log4agent`。

从 GitHub 安装：

```bash
python3 ~/.codex/skills/.system/skill-installer/scripts/install-skill-from-github.py --url https://github.com/Icyoung/log4agent/tree/main/skills/log4agent
```

也可以复制或软链到 agent 的 skill 目录后，用 `$log4agent` 调用。该 skill 会指导 agent 启动本地 server、集成 SDK，以及查看 `.log4agent/YYYY-MM-DD/<deviceId>/<sessionId>.jsonl` 日志。

Codex:

```bash
ln -s /path/to/Log4Agent/skills/log4agent ~/.codex/skills/log4agent
```

Claude:

```bash
ln -s /path/to/Log4Agent/skills/log4agent ~/.agents/skills/log4agent
```

## 查看日志

默认日志目录是运行 `log4agent-server` 命令所在目录下的 `.log4agent`。

查看 server 状态：

```bash
log4agent-server status
curl -sS http://127.0.0.1:3100/health
```

查找 session：

```bash
find .log4agent -name '*.session.jsonl' -print
```

查找日志文件：

```bash
find .log4agent -name '*.jsonl' ! -name '*.session.jsonl' -print
```

查看某个 session 的最新日志：

```bash
tail -100 .log4agent/YYYY-MM-DD/<deviceId>/<sessionId>.jsonl
```

通过 server 查询：

```bash
curl -sS 'http://127.0.0.1:3100/logs?tail=100'
curl -sS 'http://127.0.0.1:3100/logs?deviceId=<deviceId>&sessionId=<sessionId>&tail=100'
```

调试具体用户动作时，优先找最新的 `.session.jsonl`，再 tail 同目录下对应的 `<sessionId>.jsonl`。
