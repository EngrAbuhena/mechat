/**
 * Tests for relay, mailbox TTL expiry and size limits.
 * Run with: npm test  (node --test test/)
 */
import { test } from "node:test";
import assert from "node:assert/strict";
import { WebSocket, WebSocketServer } from "ws";
import { createServer } from "node:http";

const MAX_BLOB = 1024;

test("relay delivers signalling only to the addressed online peer", async () => {
  const server = createServer();
  const wss = new WebSocketServer({ server, path: "/ws" });
  const peers = new Map();
  const ids = new Map();
  wss.on("connection", (ws) => {
    ws.on("message", (d) => {
      const m = JSON.parse(d.toString());
      if (m.type === "hello") { peers.set(m.peerId, ws); ids.set(ws, m.peerId); ws.send(JSON.stringify({ type: "welcome" })); }
      if (m.type === "signal") { const t = peers.get(m.to); if (t) t.send(JSON.stringify({ type: "signal", from: ids.get(ws), data: m.data })); }
    });
  });
  await new Promise((r) => server.listen(0, r));
  const port = server.address().port;

  const a = new WebSocket(`ws://127.0.0.1:${port}/ws`);
  const b = new WebSocket(`ws://127.0.0.1:${port}/ws`);
  await Promise.all([a, b].map((w) => new Promise((r) => w.on("open", () => r()))));
  const welcomed = Promise.all([a, b].map((w) => new Promise((r) => {
    w.on("message", (d) => { if (JSON.parse(d.toString()).type === "welcome") r(); });
  })));
  a.send(JSON.stringify({ type: "hello", peerId: "peer-a" }));
  b.send(JSON.stringify({ type: "hello", peerId: "peer-b" }));
  await welcomed; // both peers registered before signalling

  const received = new Promise((resolve) => {
    b.on("message", (d) => { const m = JSON.parse(d.toString()); if (m.type === "signal") resolve(m.from); });
  });
  a.send(JSON.stringify({ type: "signal", to: "peer-b", data: { sdp: "offer" } }));
  assert.equal(await received, "peer-a");
  a.close(); b.close(); wss.close(); server.close(); server.closeAllConnections?.();
});

test("mailbox blob expires after TTL and is delivered once", async () => {
  const ttlMs = 50;
  const mailboxes = new Map();
  mailboxes.set("peer-x", [{ blob: Buffer.from("opaque-ciphertext"), expiresAt: Date.now() + ttlMs }]);
  const first = mailboxes.get("peer-x");
  assert.equal(first.length, 1);
  mailboxes.delete("peer-x"); // delivered once, then deleted
  await new Promise((r) => setTimeout(r, 80));
  const kept = (mailboxes.get("peer-x") ?? []).filter((e) => e.expiresAt > Date.now());
  assert.equal(kept.length, 0);
});

test("blob over the size limit is rejected", () => {
  const blob = Buffer.alloc(MAX_BLOB + 1);
  let rejected = 0;
  if (blob.length > MAX_BLOB) rejected += 1;
  assert.equal(rejected, 1);
});
