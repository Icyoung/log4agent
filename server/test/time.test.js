import test from "node:test";
import assert from "node:assert/strict";
import { formatLocalDate, formatLocalIso } from "../src/server.js";

test("formats receivedAt using local timezone offset instead of UTC Z", () => {
  const date = new Date(2026, 5, 5, 16, 7, 8, 9);
  const timezoneOffset = -date.getTimezoneOffset();
  const sign = timezoneOffset >= 0 ? "+" : "-";
  const absoluteOffset = Math.abs(timezoneOffset);
  const expectedOffset = `${sign}${String(Math.floor(absoluteOffset / 60)).padStart(2, "0")}:${String(absoluteOffset % 60).padStart(2, "0")}`;

  assert.equal(formatLocalIso(date), `2026-06-05T16:07:08.009${expectedOffset}`);
});

test("formats log directory date using local date", () => {
  assert.equal(formatLocalDate(new Date(2026, 5, 5, 1, 2, 3)), "2026-06-05");
});
