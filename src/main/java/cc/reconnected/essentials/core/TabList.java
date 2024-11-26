package cc.reconnected.essentials.core;

import cc.reconnected.essentials.RccEssentials;
import cc.reconnected.essentials.api.events.RccEvents;
import cc.reconnected.essentials.util.Components;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TabList {
    private static MinecraftServer server;
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static ScheduledFuture<?> scheduledFuture = null;

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(mcServer -> {
            server = mcServer;
            schedule();
        });

        RccEvents.RELOAD.register(instance -> {
            if (scheduledFuture != null) {
                scheduledFuture.cancel(false);
            }
            schedule();
        });
    }

    private static void schedule() {
        if (!RccEssentials.CONFIG.customTabList.enableTabList)
            return;

        scheduledFuture = RccEssentials.scheduler.scheduleAtFixedRate(TabList::updateTab, 0, RccEssentials.CONFIG.customTabList.tabListDelay, TimeUnit.MILLISECONDS);
    }

    public static void updateTab() {
        var period = Math.max(RccEssentials.CONFIG.customTabList.tabPhasePeriod, 1);

        var phase = (Math.sin((server.getTicks() * Math.PI * 2) / period) + 1) / 2d;

        server.getPlayerManager().getPlayerList().forEach(player -> {
            var playerContext = PlaceholderContext.of(player);
            Component headerComponent = Component.empty();
            for (int i = 0; i < RccEssentials.CONFIG.customTabList.tabHeader.size(); i++) {
                var line = RccEssentials.CONFIG.customTabList.tabHeader.get(i);
                line = line.replace("{phase}", String.valueOf(phase));
                if (i > 0) {
                    headerComponent = headerComponent.appendNewline();
                }

                headerComponent = headerComponent.append(miniMessage.deserialize(line));
            }

            Component footerComponent = Component.empty();
            for (int i = 0; i < RccEssentials.CONFIG.customTabList.tabFooter.size(); i++) {
                var line = RccEssentials.CONFIG.customTabList.tabFooter.get(i);
                line = line.replace("{phase}", String.valueOf(phase));
                if (i > 0) {
                    footerComponent = footerComponent.appendNewline();
                }

                footerComponent = footerComponent.append(miniMessage.deserialize(line));
            }

            var parsedHeader = Placeholders.parseText(Components.toText(headerComponent), playerContext);
            var parsedFooter = Placeholders.parseText(Components.toText(footerComponent), playerContext);

            var audience = RccEssentials.getInstance().adventure().player(player.getUuid());
            audience.sendPlayerListHeaderAndFooter(parsedHeader, parsedFooter);
        });
    }

}
