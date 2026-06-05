import test from "node:test";
import assert from "node:assert/strict";
import { REDACTED, redactLogEntry, redactUrl } from "../src/redactor.js";

test("redacts sensitive object keys recursively", () => {
  const result = redactLogEntry({
    authorization: "Bearer abc",
    attributes: {
      access_token: "secret",
      symbol: "BTC-USDT",
    },
  });

  assert.equal(result.authorization, REDACTED);
  assert.equal(result.attributes.access_token, REDACTED);
  assert.equal(result.attributes.symbol, "BTC-USDT");
});

test("redacts sensitive query params", () => {
  const result = redactUrl("https://example.com/path?token=abc&symbol=BTC-USDT&code=123");
  assert.equal(result, "https://example.com/path?token=[REDACTED]&symbol=BTC-USDT&code=[REDACTED]");
});

test("keeps routing fields used for device and session storage", () => {
  const result = redactLogEntry({
    deviceId: "pixel_8",
    sessionId: "session_1",
    app: "demo",
    token: "abc",
  });

  assert.equal(result.deviceId, "pixel_8");
  assert.equal(result.sessionId, "session_1");
  assert.equal(result.app, "demo");
  assert.equal(result.token, REDACTED);
});
