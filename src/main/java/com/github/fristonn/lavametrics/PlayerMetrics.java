package com.github.fristonn.lavametrics;

import dev.arbjerg.lavalink.api.IPlayer;
import lavalink.server.player.LavalinkPlayer;

public record PlayerMetrics(String guildId, long observedAtMs, boolean playing,
        boolean voiceGatewayConnected, Measurement voiceGatewayPingMs,
        FrameAvailability sourceFrames, Measurement discordLossPercent, Measurement receiverJitterMs) {

    public static PlayerMetrics snapshot(IPlayer player) {
        var unsupported = new Measurement("unsupported", null, "receiver_reports_not_exposed");
        if (!(player instanceof LavalinkPlayer nativePlayer)) {
            return new PlayerMetrics(Long.toString(player.getGuildId()), System.currentTimeMillis(),
                    player.isPlaying(), false, new Measurement("unsupported", null, "incompatible_player"),
                    new FrameAvailability("unsupported", null, null, null, "incompatible_player"),
                    unsupported, unsupported);
        }
        var connection = nativePlayer.getSocketContext().getKoe().getConnection(player.getGuildId());
        var gateway = connection == null ? null : connection.getGatewayConnection();
        boolean connected = gateway != null && gateway.isOpen();
        long ping = connected ? gateway.getPing() : -1;
        var latency = new Measurement(connected && ping >= 0 ? "available" : "unavailable",
                connected && ping >= 0 ? (double) ping : null,
                connected ? (ping >= 0 ? null : "awaiting_heartbeat") : "gateway_disconnected");
        var counter = nativePlayer.getAudioLossCounter();
        var frames = frameAvailability(player.isPlaying() && connected, counter.isDataUsable(),
                counter.getLastMinuteSuccess(), counter.getLastMinuteLoss());
        return new PlayerMetrics(Long.toString(player.getGuildId()), System.currentTimeMillis(),
                player.isPlaying(), connected, latency, frames, unsupported, unsupported);
    }

    static FrameAvailability frameAvailability(boolean active, boolean usable, int provided, int missing) {
        if (!active) return new FrameAvailability("unavailable", null, null, null, "player_inactive");
        long total = (long) provided + missing;
        if (!usable || provided < 0 || missing < 0 || total == 0) {
            return new FrameAvailability("measuring", null, null, null, "awaiting_complete_minute");
        }
        return new FrameAvailability("available", provided, missing, missing * 100.0 / total, null);
    }

    public record Measurement(String status, Double value, String reason) {}

    public record FrameAvailability(String status, Integer provided, Integer missing,
                                    Double missingPercent, String reason) {}
}
