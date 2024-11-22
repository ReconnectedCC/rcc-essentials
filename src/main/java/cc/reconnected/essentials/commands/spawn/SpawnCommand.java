package cc.reconnected.essentials.commands.spawn;

import cc.reconnected.library.text.Placeholder;
import cc.reconnected.essentials.RccEssentials;
import cc.reconnected.essentials.struct.ServerPosition;
import com.mojang.brigadier.CommandDispatcher;
import eu.pb4.placeholders.api.PlaceholderContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class SpawnCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var rootCommand = literal("spawn")
                .requires(Permissions.require("rcc.command.spawn", true))
                .executes(context -> {
                    var player = context.getSource().getPlayerOrThrow();
                    var serverState = RccEssentials.state.getServerState();
                    var playerContext = PlaceholderContext.of(player);
                    var spawnPosition = serverState.spawn;
                    if (spawnPosition == null) {
                        var server = context.getSource().getServer();
                        var spawnPos = server.getOverworld().getSpawnPos();
                        spawnPosition = new ServerPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 0, 0, server.getOverworld());
                    }

                    context.getSource().sendFeedback(() -> Placeholder.parse(
                            RccEssentials.CONFIG.textFormats.commands.spawn.teleporting,
                            playerContext
                    ), false);
                    spawnPosition.teleport(player);

                    return 1;
                });

        dispatcher.register(rootCommand);
    }
}
