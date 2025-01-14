package cc.reconnected.essentials.commands.spawn;

import cc.reconnected.essentials.RccEssentials;
import cc.reconnected.essentials.struct.ServerPosition;
import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.server.command.CommandManager.literal;

public class SetSpawnCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var rootCommand = literal("setspawn")
                .requires(Permissions.require("rcc.command.setspawn", 3))
                .executes(context -> {
                    var player = context.getSource().getPlayerOrThrow();
                    var spawnPosition = new ServerPosition(player);

                    var serverState = RccEssentials.state.getServerState();
                    serverState.spawn = spawnPosition;
                    RccEssentials.state.saveServerState();

                    player.getServerWorld().setSpawnPos(
                            new BlockPos(
                                    (int)spawnPosition.x,
                                    (int)spawnPosition.y,
                                    (int)spawnPosition.z
                            ),
                            spawnPosition.yaw
                    );

                    context.getSource().sendFeedback(() -> Text.literal("Server spawn set to ")
                            .append(Text.literal(String.format("%.1f %.1f %.1f", spawnPosition.x, spawnPosition.y, spawnPosition.z))
                                    .formatted(Formatting.GOLD))
                            .formatted(Formatting.GREEN), true);

                    return 1;
                });

        dispatcher.register(rootCommand);
    }
}
