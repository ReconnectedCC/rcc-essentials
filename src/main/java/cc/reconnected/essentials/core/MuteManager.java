package cc.reconnected.essentials.core;

import cc.reconnected.essentials.RccEssentials;
import cc.reconnected.library.text.Placeholder;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;


public class MuteManager {
    public static void register() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((signedMessage, player, parameters) -> {
            var playerState = RccEssentials.state.getPlayerState(player.getUuid());
            if (playerState.muted) {
                player.sendMessage(Placeholder.parse(RccEssentials.CONFIG.textFormats.youAreMuted));
                return false;
            }
            return true;
        });
    }
}
