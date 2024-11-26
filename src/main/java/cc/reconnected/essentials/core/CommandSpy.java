package cc.reconnected.essentials.core;

import cc.reconnected.essentials.RccEssentials;
import cc.reconnected.essentials.api.events.RccEvents;
import cc.reconnected.library.RccLibrary;
import cc.reconnected.library.text.Placeholder;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;

public class CommandSpy {
    public static void register() {
        RccEvents.PLAYER_COMMAND.register((source, command) -> {
            var parts = command.split("\\s");
            if(parts.length >= 1) {
                var cmd = parts[0];
                if(RccEssentials.CONFIG.commandSpy.ignoredCommands.contains(cmd)) {
                    return;
                }
            }

            var players = source.getServer().getPlayerManager().getPlayerList();
            var luckperms = RccLibrary.getInstance().luckPerms();
            var placeholders = Map.of(
                    "player", Text.of(source.getGameProfile().getName()),
                    "command", Text.of(command)
            );
            var message = Placeholder.parse(RccEssentials.CONFIG.commandSpy.commandSpyFormat, placeholders);
            for(var player : players) {
                var permissions = luckperms.getPlayerAdapter(ServerPlayerEntity.class).getPermissionData(player);
                var commandSpyEnabled = permissions.checkPermission("rcc.commandspy").asBoolean();

                if(commandSpyEnabled && !player.getUuid().equals(source.getUuid())) {
                    player.sendMessage(message, false);
                }
            }
        });
    }
}
