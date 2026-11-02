/**
 * MeChat relay/signalling server (Node.js + TypeScript + ws).
 *
 * Privacy rules (docs/RULES.md #5):
 *  - Clients identify only by a random temporary peer ID derived from their public key.
 *  - No accounts, no database of users.
 *  - The server NEVER sees plaintext: it relays opaque E2E-encrypted blobs only.
 *  - We do NOT log IP addresses, peer IDs or payloads — only aggregate counters.
 *  - Per-connection rate limit and max blob size.
 */
import { WebSocketServer, WebSocket } from "ws";
import { createServer } from "http";
import { randomUUID } from "crypto";

const PORT = Number(process.env.PORT ?? 8080);
const MAX_BLOB_BYTES = Number(process.env.MAX_BLOB_BYTES ?? 256 * 1024); // 256 KiB
const MAILBOX_TTL_MS = Number(process.env.MAILBOX_TTL_MS ?? 24 * 60 * 60 * 1000); // 24h
const MAILBOX_TTL_MAX_MS = Number(process.env.MAILBOX_TTL_MAX_MS ?? 7 * 24 * 60 * 60 * 1000); // 7d
const RATE_LIMIT_MESSAGES = Number(process.env.RATE_LIMIT_MESSAGES ?? 20); // per window
const RATE_LIMIT_WINDOW_MS = Number(process.env.RATE_LIMIT_WINDOW_MS ?? 1000);

// Aggregate counters only — no identifiers (RULE 5).
const counters = {
  connections: 0,
  messagesRelayed: 0,
  blobsStored: 0,
  blobsDelivered: 0,
  blobsExpired: 0,
  rejected: 0,
};

interface Peer {
  id: string; // random temporary peer ID derived client-side from the public key
  ws: WebSocket;
  windowStart: number;
  windowCount: number;
}

const peers = new Map<string, Peer>();

interface MailboxEntry {
  blob: Buffer; // opaque, E2E-encrypted client-side
  expiresAt: number;
}
const mailboxes = new Map<string, MailboxEntry[]>();

function rateLimited(p: Peer, now: number): boolean {
  if (now - p.windowStart >= RATE_LIMIT_WINDOW_MS) {
    p.windowStart = now;
    p.windowCount = 0;
  }
  p.windowCount += 1;
  return p.windowCount > RATE_LIMIT_MESSAGES;
}

function send(ws: WebSocket, msg: object) {
  if (ws.readyState === WebSocket.OPEN) ws.send(JSON.stringify(msg));
}

const httpServer = createServer((_req, res) => {
  // Health endpoint exposes ONLY aggregate counters — no identifiers, no IPs.
  res.writeHead(200, { "content-type": "application/json" });
  res.end(JSON.stringify(counters));
});

const wss = new WebSocketServer({ server: httpServer, path: "/ws" });

wss.on("connection", (ws) => {
  counters.connections += 1;
  let peer: Peer | null = null;

  ws.on("message", (data) => {
    const now = Date.now();
    if (!peer) {
      // First message must be a hello with a temporary peer ID (client-derived).
      let hello: { type?: string; peerId?: string };
      try { hello = JSON.parse(data.toString()); } catch { counters.rejected += 1; return; }
      if (hello.type !== "hello" || typeof hello.peerId !== "string" || hello.peerId.length > 64) {
        counters.rejected += 1;
        return;
      }
      peer = { id: hello.peerId, ws, windowStart: now, windowCount: 0 };
      peers.set(peer.id, peer);
      send(ws, { type: "welcome", peerId: peer.id });
      // Deliver any waiting mailbox blobs (delivered once, then deleted).
      const waiting = mailboxes.get(peer.id) ?? [];
      for (const entry of waiting) send(ws, { type: "mailbox", blob: entry.blob.toString("base64") });
      mailboxes.delete(peer.id);
      counters.blobsDelivered += waiting.length;
      return;
    }
    if (rateLimited(peer, now)) { counters.rejected += 1; return; }

    let msg: { type?: string; to?: string; blob?: string; ttlMs?: number; data?: unknown };
    try { msg = JSON.parse(data.toString()); } catch { counters.rejected += 1; return; }

    if (msg.type === "signal" && typeof msg.to === "string") {
      // (1) Relay WebRTC signalling (offer/answer/ICE) between online peers.
      const target = peers.get(msg.to);
      if (target) {
        send(target.ws, { type: "signal", from: peer.id, data: msg.data });
        counters.messagesRelayed += 1;
      }
    } else if (msg.type === "store" && typeof msg.to === "string" && typeof msg.blob === "string") {
      // (2) Blind mailbox: opaque encrypted blob for an offline peer.
      const blob = Buffer.from(msg.blob, "base64");
      if (blob.length === 0 || blob.length > MAX_BLOB_BYTES) { counters.rejected += 1; return; }
      const ttl = Math.min(Math.max(Number(msg.ttlMs) || MAILBOX_TTL_MS, 0), MAILBOX_TTL_MAX_MS) || MAILBOX_TTL_MS;
      const list = mailboxes.get(msg.to) ?? [];
      list.push({ blob, expiresAt: now + ttl });
      mailboxes.set(msg.to, list);
      counters.blobsStored += 1;
    }
  });

  ws.on("close", () => {
    if (peer) peers.delete(peer.id);
  });
});

// TTL sweeper: expire mailbox blobs (never logged, just counted).
setInterval(() => {
  const now = Date.now();
  for (const [id, list] of mailboxes) {
    const kept = list.filter((e) => {
      const expired = e.expiresAt <= now;
      if (expired) counters.blobsExpired += 1;
      return !expired;
    });
    if (kept.length === 0) mailboxes.delete(id); else mailboxes.set(id, kept);
  }
}, 60_000).unref();

httpServer.listen(PORT, () => {
  // No addresses logged beyond the port; no peer data ever.
  console.log(`mechat-server listening on port ${PORT}`);
});
