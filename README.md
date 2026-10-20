# LavaMetrics

LavaMetrics adds per-player source-frame statistics and voice gateway ping to Lavalink. It reads the player's existing state.

Built for **Lavalink 4.2.2** and its bundled **Koe 3.0.0-pre6** transport.

## Install

Download the JAR from [Releases](https://github.com/Fristonn/LavaMetrics/releases), put it in your Lavalink `plugins` folder, and restart the node. No extra configuration is needed.

To build from source, use JDK 17 and run `./gradlew build` or `.\gradlew.bat build` on Windows. The JAR will be in `build/libs`.

## Usage

```http
GET /v4/sessions/{sessionId}/players/{guildId}/metrics
Authorization: <your Lavalink password>
```

Use the session ID from Lavalink's WebSocket `ready` event and the player's guild ID. Make the request from your backend so the node password stays private.
