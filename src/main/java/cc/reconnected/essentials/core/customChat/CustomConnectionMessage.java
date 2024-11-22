package cc.reconnected.essentials.core.customChat;

import cc.reconnected.library.text.Placeholder;
import cc.reconnected.essentials.RccEssentials;
import eu.pb4.placeholders.api.PlaceholderContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;

public class CustomConnectionMessage {
    public static Text onJoin(ServerPlayerEntity player) {
        var playerContext = PlaceholderContext.of(player);
        return Placeholder.parse(
                RccEssentials.CONFIG.textFormats.joinFormat,
                playerContext
        );
    }

    public static Text onJoinRenamed(ServerPlayerEntity player, String previousName) {
        var playerContext = PlaceholderContext.of(player);
        return Placeholder.parse(
                RccEssentials.CONFIG.textFormats.joinRenamedFormat,
                playerContext,
                Map.of("previousName", Text.of(previousName))
        );
    }

    public static Text onLeave(ServerPlayerEntity player) {
        var playerContext = PlaceholderContext.of(player);
        return Placeholder.parse(
                RccEssentials.CONFIG.textFormats.leaveFormat,
                playerContext
        );
    }
}
