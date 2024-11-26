package cc.reconnected.essentials.core;

import cc.reconnected.essentials.RccEssentials;
import cc.reconnected.library.text.Placeholder;
import eu.pb4.placeholders.api.PlaceholderContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.text.Text;

public class Motd {
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if(RccEssentials.CONFIG.motd.enableMotd) {
                var motd = buildMotd(PlaceholderContext.of(handler.getPlayer()));
                handler.getPlayer().sendMessage(motd);
            }
        });
    }

    public static Text buildMotd(PlaceholderContext context) {
        var motd = String.join("\n", RccEssentials.CONFIG.motd.motdLines);

        return Placeholder.parse(motd, context);
    }
}
