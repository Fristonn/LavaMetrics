package com.github.fristonn.lavametrics;

import dev.arbjerg.lavalink.api.ISocketServer;
import java.lang.reflect.Proxy;
import java.util.Map;

public final class MetricsVerification {
    public static void main(String[] arguments) {
        var healthy = PlayerMetrics.frameAvailability(true, true, 3000, 0);
        require(healthy.status().equals("available") && healthy.missingPercent() == 0, "Healthy frames");
        var missing = PlayerMetrics.frameAvailability(true, true, 2970, 30);
        require(missing.missingPercent() == 1, "Frame percentage");
        require(PlayerMetrics.frameAvailability(true, true, 0, 0).missingPercent() == null, "Empty window");
        require(PlayerMetrics.frameAvailability(true, false, 3000, 0).provided() == null, "Incomplete window");
        require(PlayerMetrics.frameAvailability(false, true, 3000, 0).provided() == null, "Inactive player");
        require(PlayerMetrics.frameAvailability(true, true, -1, 1).provided() == null, "Invalid counter");
        var large = PlayerMetrics.frameAvailability(true, true, Integer.MAX_VALUE, Integer.MAX_VALUE);
        require(large.missingPercent() == 50, "Counter overflow protection");
        var server = (ISocketServer) Proxy.newProxyInstance(ISocketServer.class.getClassLoader(),
                new Class<?>[]{ISocketServer.class}, (proxy, method, parameters) -> {
                    if (method.getName().equals("getSessions") || method.getName().equals("getResumableSessions")) {
                        return Map.of();
                    }
                    throw new AssertionError("Unexpected mutation: " + method.getName());
                });
        var controller = new MetricsController(server);
        require(controller.fetchMetrics("session", "123").getStatusCode().value() == 404, "Missing player");
        for (String guildId : new String[]{"0", "-1", "1.0", "01", "abc", "9999999999999999999"}) {
            require(controller.fetchMetrics("session", guildId).getStatusCode().value() == 400, "Invalid guild");
        }
        require(controller.fetchMetrics("s".repeat(129), "123").getStatusCode().value() == 400, "Session bound");
        require("no-store".equals(controller.fetchMetrics("session", "123").getHeaders().getCacheControl()),
                "Cache prevention");
        System.out.println("LavaMetrics verification passed");
    }

    private static void require(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
}
