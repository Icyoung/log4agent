# log4agent-server

Local mobile log collector for coding agents.

`log4agent-server` runs on your development machine, receives structured logs from mobile apps over HTTP, stores them as JSONL by device and session, and can print incoming logs in the foreground. It is designed for Android emulator, iOS simulator, Flutter, native Android/iOS, and Kotlin Multiplatform debugging workflows where a coding agent needs a simple way to inspect mobile runtime logs.

## Install

```bash
npm install -g log4agent-server
```

## Start

Run in the foreground and print logs as they arrive:

```bash
log4agent-server start --port 3100
```

Run in the background:

```bash
log4agent-server start --background --port 3100
```

Check or stop the background server:

```bash
log4agent-server status
log4agent-server stop
```

By default, logs are written to `.hostlogs` under the terminal working directory.

## Client Defaults

Mobile clients should post logs to:

- Android emulator: `http://10.0.2.2:3100/logs`
- iOS simulator: `http://127.0.0.1:3100/logs`
- Real devices: use your machine's LAN IP, for example `http://192.168.1.10:3100/logs`

## Endpoints

- `GET /health`
- `POST /sessions`
- `POST /logs`
- `GET /logs?tail=100`
- `GET /logs?deviceId=<device>&sessionId=<session>&tail=100`

## Storage

Logs are stored as JSONL:

```text
.hostlogs/
  2026-06-05/
    pixel_8/
      1749100000000-abcd.session.jsonl
      1749100000000-abcd.jsonl
```

Each log entry includes `app`, `deviceId`, and `sessionId`. A session is created when the client SDK is configured and performs its `/sessions` handshake.

## Configuration

CLI options:

```bash
log4agent-server start --port 3100 --dir .hostlogs
log4agent-server start --background --no-redact
```

Environment variables:

- `LOG4AGENT_PORT`: server port, default `3100`
- `LOG4AGENT_DIR`: output directory, default `.hostlogs`
- `LOG4AGENT_REDACT`: set to `false` to disable server-side redaction
- `LOG4AGENT_PID_FILE`: optional background pid file
- `LOG4AGENT_OUT_FILE`: optional background stdout/stderr file

## Agent Usage

For coding agents, start the server before reproducing the mobile issue:

```bash
log4agent-server start --background
curl "http://127.0.0.1:3100/logs?tail=200"
```

Then inspect `.hostlogs` or query `/logs` to correlate logs with the reproduced action.

## Repository

Full documentation and mobile client packages:

https://github.com/Icyoung/log4agent
