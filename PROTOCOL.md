# Mesh Protocol & Packet Format

> Design specification. Implementation lands across **Milestone 5 (routing)**
> and **Milestone 6 (encryption)**. This is an original protocol — it is not
> a reimplementation of any proprietary mesh protocol, and is built only on
> public Android BLE APIs (GATT characteristics for framed transport).

## Design Goals

- Small enough to fit BLE MTU-constrained transport (fragmentation-aware).
- Every packet authenticated (Ed25519 signature) and, for private
  messages, encrypted (ChaCha20-Poly1305).
- Stateless-per-hop relay: a relaying node needs no knowledge of the
  original sender beyond what's in the packet header.
- Replay-resistant via message ID + timestamp + seen-packet cache.

## Packet Layout

All multi-byte integers are big-endian.

| Field             | Size (bytes) | Description |
|-------------------|:---:|-------------|
| Version           | 1   | Protocol version, starts at `0x01`. |
| Packet Type       | 1   | `0x01` Private message, `0x02` Channel message, `0x03` Ack, `0x04` Peer announce, `0x05` Key rotation notice. |
| Flags             | 1   | Bitfield: bit0 = encrypted, bit1 = fragmented, bit2 = ack-requested, bits3–7 reserved. |
| TTL               | 1   | Remaining relay hops (decremented per hop; dropped at 0). |
| Hop Count         | 1   | Hops traversed so far (incremented per relay, for diagnostics). |
| Sender            | 32  | Sender's Ed25519 public key (identity). |
| Destination       | 32  | Recipient's Ed25519 public key, or all-zero for channel/broadcast. |
| Message ID        | 16  | Random UUID, unique per logical message (stable across fragments/relays). |
| Timestamp         | 8   | Unix epoch milliseconds, origination time (used for replay/expiry checks). |
| Payload Length    | 2   | Length in bytes of the (possibly encrypted) payload that follows. |
| Encrypted Payload | variable | ChaCha20-Poly1305 ciphertext (private messages) or plaintext body (signed channel messages) — see Encryption below. |
| Signature         | 64  | Ed25519 signature over all preceding fields. |
| CRC               | 4   | CRC32 over the entire packet (transport-layer corruption check, independent of the cryptographic signature). |

Fixed header overhead (everything except payload): **162 bytes**.

## Packet Types

- **`0x01` Private message** — payload encrypted with a key derived via
  X25519 ECDH between sender and destination (HKDF-derived session key),
  then sealed with ChaCha20-Poly1305.
- **`0x02` Channel message** — payload is plaintext (channel messages are
  public by design) but still signed; encrypted-flag bit is unset.
- **`0x03` Ack** — delivery acknowledgement referencing a `Message ID`.
- **`0x04` Peer announce** — periodic identity/nickname/capability
  broadcast used for discovery and neighbour table maintenance.
- **`0x05` Key rotation notice** — signed announcement of a new public
  key, signed by the *previous* key to preserve trust continuity.

## Routing Rules

1. On receipt, verify `CRC`, then verify `Signature` against `Sender`.
   Reject on failure — never relay unverified packets.
2. Compute a dedup key from `Message ID` (+ `Sender`) and check the LRU
   `SeenPacket` cache. If already seen, drop silently (no relay, no
   processing).
3. If `Destination` matches this node's identity (or packet is a channel
   type this node subscribes to), deliver locally and optionally emit an
   Ack.
4. Otherwise, if `TTL > 0`: decrement `TTL`, increment `Hop Count`, insert
   into `SeenPacket` cache, and relay to all currently connected peers
   except the one it was received from.
5. If `TTL == 0`: drop and record a `packets_dropped_ttl` statistic.
6. Expired packets (by `Timestamp`, using a configurable max-age) are
   dropped regardless of TTL, to bound store-and-forward staleness.

## Duplicate Suppression

- `SeenPacket` is an LRU-bounded cache (size configurable; default sized
  to balance memory vs. mesh density) keyed by `Message ID`.
- Entries expire either by LRU eviction or by TTL-based aging tied to
  `Timestamp`.

## Neighbour Quality Score

Each directly-connected peer accrues a quality score based on:
- Connection stability (drop/reconnect frequency).
- RSSI trend.
- Successful relay/ack ratio.

Used to prefer higher-quality neighbours when multiple relay paths exist
and to deprioritize flaky connections for battery efficiency.

## Encryption Summary

See [SECURITY](SECURITY.md) for the vulnerability-reporting process. See
[ARCHITECTURE.md](ARCHITECTURE.md) for how the crypto engine fits into the
data layer. Key points:

- **X25519** — Diffie-Hellman key agreement per sender/recipient pair.
- **HKDF (SHA-256)** — derives a per-message or per-session symmetric key
  from the X25519 shared secret.
- **ChaCha20-Poly1305** — authenticated encryption of private message
  payloads.
- **Ed25519** — signs every packet header + payload; verified by every
  node before processing or relaying.
- **Replay rejection** — enforced via `SeenPacket` cache + timestamp
  window, independent of the AEAD's own nonce handling.

Full algorithmic detail and parameter choices will be finalized and
documented here as Milestone 6 is implemented.
