package cc.reconnected.essentials.commands.misc;

import cc.reconnected.essentials.core.AfkTracker;
import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class AfkCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var rootCommand = literal("afk")
                .requires(Permissions.require("rcc.command.afk", true))
                .executes(context -> {
                    var player = context.getSource().getPlayerOrThrow();
                    AfkTracker.getInstance().setPlayerAfk(player, true);

                    return 1;
                });

        dispatcher.register(rootCommand);
    }
}
