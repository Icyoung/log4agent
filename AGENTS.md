# Log4Agent Agent Guide

Use the `log4agent` skill when a task involves installing Log4Agent, integrating a mobile client SDK, running the local log server, or inspecting mobile logs.

Install or update the skill from GitHub:

```bash
python3 ~/.codex/skills/.system/skill-installer/scripts/install-skill-from-github.py --url https://github.com/Icyoung/log4agent/tree/main/skills/log4agent
```

After installing the skill, restart Codex so the skill registry reloads.

Operational defaults:

- Server npm package: `log4agent-server`
- Default port: `3100`
- Default log directory: `.hostlogs` under the terminal working directory
- Android emulator endpoint: `http://10.0.2.2:3100/logs`
- iOS simulator endpoint: `http://127.0.0.1:3100/logs`

When debugging a mobile issue, prefer session-scoped logs under `.hostlogs/YYYY-MM-DD/<deviceId>/<sessionId>.jsonl`.
