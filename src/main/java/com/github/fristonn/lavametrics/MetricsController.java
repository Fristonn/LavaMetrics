package com.github.fristonn.lavametrics;

import dev.arbjerg.lavalink.api.ISocketServer;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public final class MetricsController {
    private final ISocketServer socketServer;

    public MetricsController(ISocketServer socketServer) {
        this.socketServer = socketServer;
    }

    @GetMapping("/v4/sessions/{sessionId}/players/{guildId}/metrics")
    public ResponseEntity<?> fetchMetrics(@PathVariable String sessionId, @PathVariable String guildId) {
        if (sessionId.length() > 128 || !guildId.matches("[1-9][0-9]{0,18}")) {
            return respond(400, Map.of("error", "Invalid player identifier"));
        }
        long parsedGuildId;
        try {
            parsedGuildId = Long.parseLong(guildId);
        } catch (NumberFormatException exception) {
            return respond(400, Map.of("error", "Invalid player identifier"));
        }
        var context = socketServer.getSessions().get(sessionId);
        if (context == null) context = socketServer.getResumableSessions().get(sessionId);
        var player = context == null ? null : context.getPlayers().get(parsedGuildId);
        if (player == null) return respond(404, Map.of("error", "Player not found"));
        return respond(200, PlayerMetrics.snapshot(player));
    }

    private static ResponseEntity<?> respond(int status, Object body) {
        return ResponseEntity.status(status).cacheControl(CacheControl.noStore()).body(body);
    }
}
