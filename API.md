# REST API (Local Bridge)

> Design specification. Implementation lands in **Milestone 8 — REST
> Bridge**. All endpoints are bound to `127.0.0.1` only and are never
> exposed on a network interface.

Base URL (once implemented): `http://127.0.0.1:<port>` (default port to be
finalized in Milestone 8; likely a fixed high port such as `8765`).

All responses are JSON. All timestamps are Unix epoch milliseconds (UTC).

---

## `GET /status`

Returns device and mesh node status.

**Response**
```json
{
  "device_id": "string (UUID)",
  "nickname": "string",
  "ble_state": "advertising | scanning | idle | disabled",
  "uptime_seconds": 0,
  "battery_optimized": true
}
```

## `GET /peers`

Lists currently known/connected peers.

**Response**
```json
{
  "peers": [
    {
      "peer_id": "string (UUID)",
      "nickname": "string",
      "rssi": -60,
      "last_seen": 0,
      "quality_score": 0.0,
      "connected": true
    }
  ]
}
```

## `GET /messages`

Retrieves stored messages (optionally filtered).

**Query params:** `since` (timestamp, optional), `channel` (string,
optional), `peer_id` (string, optional).

**Response**
```json
{
  "messages": [
    {
      "message_id": "string",
      "sender_id": "string",
      "destination_id": "string | null",
      "channel": "string | null",
      "body_ciphertext": "base64 string",
      "timestamp": 0,
      "hop_count": 0,
      "delivery_status": "pending | relayed | delivered | failed"
    }
  ]
}
```

## `POST /send`

Sends a new message (private or channel/public).

**Request**
```json
{
  "destination_id": "string | null",
  "channel": "string | null",
  "body": "string",
  "ttl": 5
}
```

**Response**
```json
{
  "message_id": "string",
  "accepted": true
}
```

## `GET /statistics`

Mesh/routing statistics.

**Response**
```json
{
  "packets_sent": 0,
  "packets_relayed": 0,
  "packets_dropped_duplicate": 0,
  "packets_dropped_ttl": 0,
  "peers_seen_total": 0,
  "avg_hop_count": 0.0
}
```

## `GET /logs`

Exports diagnostic logs.

**Query params:** `limit` (int, optional, default 500).

**Response**
```json
{
  "logs": [
    { "timestamp": 0, "level": "INFO | WARN | ERROR", "message": "string" }
  ]
}
```

## `POST /identity`

Manages local identity (nickname update, key rotation trigger). Never
returns private key material.

**Request**
```json
{
  "nickname": "string | null",
  "rotate_keys": false
}
```

**Response**
```json
{
  "device_id": "string",
  "nickname": "string",
  "public_key_x25519": "base64 string",
  "public_key_ed25519": "base64 string",
  "rotated": false
}
```

---

## Errors

Standard HTTP status codes. Error body shape:

```json
{ "error": "string (machine-readable code)", "detail": "string" }
```

## Authentication

Since the bridge is loopback-only, it relies on OS-level process
isolation. A future milestone may add a shared local token if deemed
necessary — tracked in [ROADMAP.md](ROADMAP.md).
