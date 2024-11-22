package cc.reconnected.essentials.core.customChat;

import cc.reconnected.essentials.RccEssentials;
import cc.reconnected.library.text.Placeholder;
import eu.pb4.placeholders.api.PlaceholderContext;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;

public class CustomDeathMessage {
    public static Text onDeath(ServerPlayerEntity player, DamageTracker instance) {
        var deathMessage = instance.getDeathMessage();
        var playerContext = PlaceholderContext.of(player);

        return Placeholder.parse(
                RccEssentials.CONFIG.textFormats.deathFormat,
                playerContext,
                Map.of("message", deathMessage)
        );
    }
}
