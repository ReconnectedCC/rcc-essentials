package cc.reconnected.essentials.commands.teleport;

import cc.reconnected.essentials.RccEssentials;
import cc.reconnected.library.text.Placeholder;
import cc.reconnected.essentials.core.BackTracker;
import com.mojang.brigadier.CommandDispatcher;
import eu.pb4.placeholders.api.PlaceholderContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class BackCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var rootCommand = literal("back")
                .requires(Permissions.require("rcc.command.back", true))
                .executes(context -> {
                    var player = context.getSource().getPlayerOrThrow();
                    var playerContext = PlaceholderContext.of(player);

                    var lastPosition = BackTracker.lastPlayerPositions.get(player.getUuid());
                    if (lastPosition == null) {
                        context.getSource().sendFeedback(() -> Placeholder.parse(
                                RccEssentials.CONFIG.textFormats.commands.back.noPosition,
                                playerContext
                        ), false);
                        return 1;
                    }

                    context.getSource().sendFeedback(() -> Placeholder.parse(
                            RccEssentials.CONFIG.textFormats.commands.back.teleporting,
                            playerContext
                    ), false);
                    lastPosition.teleport(player);

                    return 1;
                });

        dispatcher.register(rootCommand);
    }
}
