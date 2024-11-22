package cc.reconnected.essentials.core;

import cc.reconnected.essentials.RccEssentials;
import cc.reconnected.essentials.util.Components;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class TabList {
    public static void register() {
        if (!RccEssentials.CONFIG.customTabList.enableTabList)
            return;

        var minimessage = MiniMessage.miniMessage();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            var delay = Math.max(RccEssentials.CONFIG.customTabList.tabListTickDelay, 1);
            if(server.getTicks() % delay == 0) {
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

                        headerComponent = headerComponent.append(minimessage.deserialize(line));
                    }

                    Component footerComponent = Component.empty();
                    for (int i = 0; i < RccEssentials.CONFIG.customTabList.tabFooter.size(); i++) {
                        var line = RccEssentials.CONFIG.customTabList.tabFooter.get(i);
                        line = line.replace("{phase}", String.valueOf(phase));
                        if (i > 0) {
                            footerComponent = footerComponent.appendNewline();
                        }

                        footerComponent = footerComponent.append(minimessage.deserialize(line));
                    }

                    var parsedHeader = Placeholders.parseText(Components.toText(headerComponent), playerContext);
                    var parsedFooter = Placeholders.parseText(Components.toText(footerComponent), playerContext);

                    var audience = RccEssentials.getInstance().adventure().player(player.getUuid());
                    audience.sendPlayerListHeaderAndFooter(parsedHeader, parsedFooter);
                });
            }
        });

    }

}
