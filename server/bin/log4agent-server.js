#!/usr/bin/env node

import fs from "node:fs";
import path from "node:path";
import { spawn } from "node:child_process";
import { startServer } from "../src/server.js";

const args = process.argv.slice(2);
const command = args[0] && !args[0].startsWith("-") ? args[0] : "start";
const commandArgs = command === args[0] ? args.slice(1) : args;

function valueAfter(name, fallback) {
  const index = commandArgs.indexOf(name);
  if (index < 0) return fallback;
  return commandArgs[index + 1] ?? fallback;
}

function hasFlag(name) {
  return commandArgs.includes(name);
}

function optionsFromArgs() {
  const logDir = valueAfter("--dir", process.env.LOG4AGENT_DIR || ".hostlogs");
  return {
    port: Number(valueAfter("--port", process.env.LOG4AGENT_PORT || process.env.PORT || 3100)),
    logDir,
    redactionEnabled: !hasFlag("--no-redact") && process.env.LOG4AGENT_REDACT !== "false",
    pidFile: valueAfter("--pid-file", process.env.LOG4AGENT_PID_FILE || path.join(logDir, "server.pid")),
    outFile: valueAfter("--out-file", process.env.LOG4AGENT_OUT_FILE || path.join(logDir, "server.log")),
  };
}

const options = optionsFromArgs();

switch (command) {
  case "start":
    if (hasFlag("--background") || hasFlag("-d")) {
      startBackground(options);
    } else {
      startServer({ ...options, printLogs: true });
    }
    break;
  case "stop":
    stopBackground(options);
    break;
  case "status":
    printStatus(options);
    break;
  default:
    printHelpAndExit();
}

function startBackground(options) {
  fs.mkdirSync(path.dirname(options.pidFile), { recursive: true });
  fs.mkdirSync(path.dirname(options.outFile), { recursive: true });

  const existingPid = readPid(options.pidFile);
  if (existingPid && isRunning(existingPid)) {
    console.log(`[log4agent-server] already running pid=${existingPid}`);
    return;
  }

  const childArgs = [
    new URL(import.meta.url).pathname,
    "start",
    "--port",
    String(options.port),
    "--dir",
    options.logDir,
    "--pid-file",
    options.pidFile,
    "--out-file",
    options.outFile,
  ];
  if (!options.redactionEnabled) childArgs.push("--no-redact");

  const out = fs.openSync(options.outFile, "a");
  const child = spawn(process.execPath, childArgs, {
    detached: true,
    stdio: ["ignore", out, out],
    env: process.env,
  });

  fs.writeFileSync(options.pidFile, `${child.pid}\n`, "utf8");
  child.unref();

  console.log(`[log4agent-server] started in background pid=${child.pid}`);
  console.log(`[log4agent-server] writing process output to ${path.resolve(options.outFile)}`);
}

function stopBackground(options) {
  const pid = readPid(options.pidFile);
  if (!pid) {
    console.log("[log4agent-server] not running: pid file not found");
    return;
  }
  if (!isRunning(pid)) {
    fs.rmSync(options.pidFile, { force: true });
    console.log(`[log4agent-server] stale pid removed pid=${pid}`);
    return;
  }
  process.kill(pid, "SIGTERM");
  fs.rmSync(options.pidFile, { force: true });
  console.log(`[log4agent-server] stopped pid=${pid}`);
}

function printStatus(options) {
  const pid = readPid(options.pidFile);
  if (pid && isRunning(pid)) {
    console.log(`[log4agent-server] running pid=${pid}`);
    return;
  }
  if (pid) {
    console.log(`[log4agent-server] stopped stalePid=${pid}`);
    return;
  }
  console.log("[log4agent-server] stopped");
}

function readPid(pidFile) {
  try {
    const value = fs.readFileSync(pidFile, "utf8").trim();
    return value ? Number(value) : null;
  } catch {
    return null;
  }
}

function isRunning(pid) {
  try {
    process.kill(pid, 0);
    return true;
  } catch {
    return false;
  }
}

function printHelpAndExit() {
  console.error(`Usage:
  log4agent-server start [--background] [--port 3100] [--dir .hostlogs]
  log4agent-server stop
  log4agent-server status`);
  process.exit(1);
}
