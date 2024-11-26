package cc.reconnected.essentials.core;

import cc.reconnected.essentials.RccEssentials;
import cc.reconnected.essentials.api.events.RccEvents;
import cc.reconnected.library.text.Placeholder;
import eu.pb4.placeholders.api.PlaceholderContext;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AutoAnnouncements {
    private static ScheduledFuture<?> scheduledFuture = null;
    private static int currentLine = 0;
    private static MinecraftServer server;
    public static void register() {
        RccEvents.RELOAD.register(instance -> {
            if(scheduledFuture != null) {
                scheduledFuture.cancel(false);
            }
            setup();
        });

        ServerLifecycleEvents.SERVER_STARTED.register(mcServer -> {
            server = mcServer;
            setup();
        });
    }

    public static void announce() {
        var lines = RccEssentials.CONFIG.autoAnnouncements.announcements;
        if(lines.isEmpty())
            return;

        if(RccEssentials.CONFIG.autoAnnouncements.pickRandomly) {
            currentLine = new Random().nextInt(lines.size());
        } else {
            currentLine = currentLine++ % lines.size();
        }

        var line = lines.get(currentLine);
        var serverContext = PlaceholderContext.of(server);
        RccEssentials.getInstance().broadcast(Placeholder.parse(line, serverContext));
    }

    private static void setup() {
        currentLine = 0;
        if(RccEssentials.CONFIG.autoAnnouncements.enableAnnouncements) {
            var delay = RccEssentials.CONFIG.autoAnnouncements.delay;
            scheduledFuture = RccEssentials.scheduler.scheduleAtFixedRate(AutoAnnouncements::announce, delay, delay, TimeUnit.SECONDS);
        }
    }
}
