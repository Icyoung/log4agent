export const REDACTED = "[REDACTED]";

export const defaultSensitiveKeys = [
  "authorization",
  "auth",
  "bearer",
  "token",
  "access_token",
  "refresh_token",
  "password",
  "passwd",
  "secret",
  "cookie",
  "set-cookie",
  "session",
  "code",
];

export function redactLogEntry(entry, sensitiveKeys = defaultSensitiveKeys) {
  return redactValue(entry, sensitiveKeys);
}

function redactValue(value, sensitiveKeys) {
  if (Array.isArray(value)) return value.map((item) => redactValue(item, sensitiveKeys));
  if (value && typeof value === "object") {
    return Object.fromEntries(
      Object.entries(value).map(([key, nested]) => [
        key,
        isSensitiveKey(key, sensitiveKeys) ? REDACTED : redactValue(nested, sensitiveKeys),
      ]),
    );
  }
  if (typeof value === "string") return redactUrl(value, sensitiveKeys);
  return value;
}

export function redactUrl(value, sensitiveKeys = defaultSensitiveKeys) {
  if (!value.includes("?") && !value.includes("&")) return value;
  let redacted = value;
  for (const key of sensitiveKeys) {
    const pattern = new RegExp(`([?&]${escapeRegExp(key)}=)[^&#\\s]*`, "gi");
    redacted = redacted.replace(pattern, `$1${REDACTED}`);
  }
  return redacted;
}

function isSensitiveKey(key, sensitiveKeys) {
  const normalized = key.toLowerCase();
  if (["sessionid", "deviceid", "appid", "app", "platform"].includes(normalized)) return false;
  return sensitiveKeys.some((sensitive) => {
    const candidate = sensitive.toLowerCase();
    return normalized === candidate || normalized.includes(candidate);
  });
}

function escapeRegExp(value) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}
