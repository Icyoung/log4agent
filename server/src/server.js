import http from "node:http";
import fs from "node:fs";
import path from "node:path";
import { redactLogEntry } from "./redactor.js";

export function startServer(options = {}) {
  const port = Number(options.port || 3100);
  const logDir = path.resolve(options.logDir || ".hostlogs");
  const redactionEnabled = options.redactionEnabled !== false;
  const printLogs = options.printLogs !== false;

  fs.mkdirSync(logDir, { recursive: true });

  const server = http.createServer(async (req, res) => {
    try {
      if (req.method === "GET" && req.url === "/health") {
        sendJson(res, 200, { ok: true, logDir, redactionEnabled });
        return;
      }

      if (req.method === "GET" && req.url.startsWith("/logs")) {
        const url = new URL(req.url, `http://${req.headers.host || "localhost"}`);
        const lines = parseTail(url.searchParams.get("tail"));
        const deviceId = url.searchParams.get("deviceId");
        const sessionId = url.searchParams.get("sessionId");
        const files = resolveLogFiles(logDir, deviceId, sessionId);
        sendJson(res, 200, { files, logs: tailFiles(files, lines) });
        return;
      }

      if (req.method === "POST" && req.url === "/sessions") {
        const body = await readBody(req);
        const session = normalizeSession(body, req);
        const finalSession = redactionEnabled ? redactLogEntry(session) : session;
        const file = sessionFile(logDir, finalSession.deviceId, finalSession.sessionId);
        fs.mkdirSync(path.dirname(file), { recursive: true });
        fs.appendFileSync(file, `${JSON.stringify(finalSession)}\n`, "utf8");
        if (printLogs) console.log(JSON.stringify({ type: "session", ...finalSession }));
        sendJson(res, 202, { ok: true, sessionId: finalSession.sessionId, deviceId: finalSession.deviceId });
        return;
      }

      if (req.method === "POST" && req.url === "/logs") {
        const body = await readBody(req);
        const entry = normalizeLog(body, req);
        const finalEntry = redactionEnabled ? redactLogEntry(entry) : entry;
        const file = logFile(logDir, finalEntry.deviceId, finalEntry.sessionId);
        fs.mkdirSync(path.dirname(file), { recursive: true });
        fs.appendFileSync(file, `${JSON.stringify(finalEntry)}\n`, "utf8");
        if (printLogs) console.log(JSON.stringify(finalEntry));
        sendJson(res, 202, { ok: true });
        return;
      }

      sendJson(res, 404, { ok: false, error: "not_found" });
    } catch (error) {
      sendJson(res, 400, { ok: false, error: error.message || String(error) });
    }
  });

  server.listen(port, "0.0.0.0", () => {
    console.log(`[log4agent-server] listening on http://0.0.0.0:${port}`);
    console.log(`[log4agent-server] writing JSONL logs to ${logDir}`);
    console.log(`[log4agent-server] redaction ${redactionEnabled ? "enabled" : "disabled"}`);
  });

  return server;
}

function dayDir(logDir) {
  const day = new Date().toISOString().slice(0, 10);
  return path.join(logDir, day);
}

function logFile(logDir, deviceId, sessionId) {
  return path.join(dayDir(logDir), safePathPart(deviceId), `${safePathPart(sessionId)}.jsonl`);
}

function sessionFile(logDir, deviceId, sessionId) {
  return path.join(dayDir(logDir), safePathPart(deviceId), `${safePathPart(sessionId)}.session.jsonl`);
}

function safePathPart(value) {
  return String(value || "unknown")
    .replace(/[^a-zA-Z0-9._-]/g, "_")
    .slice(0, 120) || "unknown";
}

function sendJson(res, status, body) {
  const payload = JSON.stringify(body);
  res.writeHead(status, {
    "content-type": "application/json; charset=utf-8",
    "content-length": Buffer.byteLength(payload),
  });
  res.end(payload);
}

function readBody(req, limitBytes = 1024 * 1024) {
  return new Promise((resolve, reject) => {
    let size = 0;
    const chunks = [];
    req.on("data", (chunk) => {
      size += chunk.length;
      if (size > limitBytes) {
        reject(new Error("request body too large"));
        req.destroy();
        return;
      }
      chunks.push(chunk);
    });
    req.on("end", () => resolve(Buffer.concat(chunks).toString("utf8")));
    req.on("error", reject);
  });
}

function normalizeLog(raw, req) {
  const parsed = raw.trim() ? JSON.parse(raw) : {};
  return {
    receivedAt: new Date().toISOString(),
    remoteAddress: req.socket.remoteAddress,
    app: parsed.app || "log4agent",
    deviceId: parsed.deviceId || parsed.platform || "unknown-device",
    sessionId: parsed.sessionId || "no-session",
    ...parsed,
  };
}

function normalizeSession(raw, req) {
  const parsed = raw.trim() ? JSON.parse(raw) : {};
  const sessionId = parsed.sessionId || `${Date.now()}-${Math.random().toString(36).slice(2)}`;
  const deviceId = parsed.deviceId || parsed.platform || "unknown-device";
  return {
    receivedAt: new Date().toISOString(),
    remoteAddress: req.socket.remoteAddress,
    type: "session",
    app: parsed.app || "log4agent",
    deviceId,
    sessionId,
    ...parsed,
  };
}

function resolveLogFiles(logDir, deviceId, sessionId) {
  if (deviceId && sessionId) return [logFile(logDir, deviceId, sessionId)];
  const root = dayDir(logDir);
  if (!fs.existsSync(root)) return [];
  const files = [];
  const devices = deviceId ? [safePathPart(deviceId)] : fs.readdirSync(root);
  for (const device of devices) {
    const deviceDir = path.join(root, device);
    if (!fs.existsSync(deviceDir) || !fs.statSync(deviceDir).isDirectory()) continue;
    for (const name of fs.readdirSync(deviceDir)) {
      if (name.endsWith(".jsonl") && !name.endsWith(".session.jsonl")) {
        files.push(path.join(deviceDir, name));
      }
    }
  }
  return files.sort();
}

function tailFiles(files, maxLines) {
  return files.flatMap((file) => tailFile(file, maxLines)).slice(-maxLines);
}

function parseTail(value) {
  const parsed = Number(value || 100);
  if (!Number.isFinite(parsed) || parsed <= 0) return 100;
  return Math.min(Math.floor(parsed), 1000);
}

function tailFile(file, maxLines) {
  if (!fs.existsSync(file)) return [];
  const content = fs.readFileSync(file, "utf8").trim();
  if (!content) return [];
  return content.split("\n").slice(-maxLines).map((line) => {
    try {
      return JSON.parse(line);
    } catch {
      return { raw: line };
    }
  });
}
