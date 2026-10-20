# LavaMetrics

A Lavalink plugin that exposes per-player source-frame statistics and voice gateway ping. It reads existing player state without changing playback.

Supports Lavalink **4.2.2** with bundled Koe **3.0.0-pre6**. Source-frame misses are not network packet loss, and gateway ping is not audio latency. Discord-side loss and receiver jitter are currently unsupported.

## Installation

Build with JDK 17:

```sh
./gradlew build
```

On Windows:

```powershell
.\gradlew.bat build
```

Copy `build/libs/lavametrics-1.0.0.jar` into your Lavalink `plugins` directory, then restart Lavalink. No additional configuration is required.

## Endpoint

```http
GET /v4/sessions/{sessionId}/players/{guildId}/metrics
Authorization: <your Lavalink password>
```

Use the session ID from Lavalink's WebSocket `ready` event and an existing player's guild ID. Keep the node password on your backend. Unavailable measurements return `null`, not zero.
