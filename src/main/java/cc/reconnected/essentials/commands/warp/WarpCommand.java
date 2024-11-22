package cc.reconnected.essentials.commands.warp;

import cc.reconnected.library.text.Placeholder;
import cc.reconnected.essentials.RccEssentials;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.placeholders.api.PlaceholderContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class WarpCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var rootCommand = literal("warp")
                .requires(Permissions.require("rcc.command.warp", true))
                .then(argument("name", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            if (!context.getSource().isExecutedByPlayer())
                                return CommandSource.suggestMatching(new String[]{}, builder);

                            var serverState = RccEssentials.state.getServerState();
                            return CommandSource.suggestMatching(serverState.warps.keySet().stream(), builder);
                        })
                        .executes(context -> execute(context, StringArgumentType.getString(context, "name"))));

        dispatcher.register(rootCommand);
    }

    private static int execute(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrThrow();
        var serverState = RccEssentials.state.getServerState();
        var warps = serverState.warps;
        var playerContext = PlaceholderContext.of(player);

        if (!warps.containsKey(name)) {
            context.getSource().sendFeedback(() -> Placeholder.parse(
                    RccEssentials.CONFIG.textFormats.commands.warp.warpNotFound,
                    playerContext
            ), false);
            return 1;
        }

        context.getSource().sendFeedback(() -> Placeholder.parse(
                RccEssentials.CONFIG.textFormats.commands.warp.teleporting,
                playerContext,
                Map.of(
                        "warp", Text.of(name)
                )
        ), false);

        var warpPosition = warps.get(name);
        warpPosition.teleport(player);

        return 1;
    }
}