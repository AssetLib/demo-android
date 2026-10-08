# Preview security

This demo stores only the public connection JSON in app-private, non-backed-up storage. It has no editing credentials or built-in sample admin account. Published image delivery is public. Do not use confidential artwork in the hosted preview.

The native SDK verifies pinned Ed25519 signatures, exact app identity and delivery paths, monotonic sequence, image hashes/bytes, and bounded native image decoding. It uses HTTPS without redirects and maintains per-connection cache/state bounds. See the SDK's SECURITY.md for limitations, especially app-data rollback, key rotation, cache eviction, and custom storage.

The public configuration selects an HTTPS delivery origin, so paste only configurations you intend this device to contact. No arbitrary auth endpoint is accepted. Disconnect removes configuration but deliberately retains replay protection. Clearing app data resets all local state.

Use GitHub private vulnerability reporting where enabled. If unavailable, open a minimal issue requesting a private reporting channel. Do not publish secrets or exploitable details in an issue. No security response SLA is promised.
