package cc.reconnected.essentials.commands.home;

import cc.reconnected.essentials.RccEssentials;
import cc.reconnected.library.text.Placeholder;
import cc.reconnected.essentials.struct.ServerPosition;
import cc.reconnected.essentials.util.Components;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.placeholders.api.PlaceholderContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SetHomeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var rootCommand = literal("sethome")
                .requires(Permissions.require("rcc.command.sethome", true))
                .executes(context -> execute(context,
                        "home",
                        false))
                .then(argument("name", StringArgumentType.word())
                        .executes(context -> execute(context,
                                StringArgumentType.getString(context, "name"),
                                false))
                        .then(argument("force", BoolArgumentType.bool())
                                .executes(context -> execute(context,
                                        StringArgumentType.getString(context, "name"),
                                        BoolArgumentType.getBool(context, "force")))));

        dispatcher.register(rootCommand);
    }

    private static int execute(CommandContext<ServerCommandSource> context, String name, boolean forced) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrThrow();
        var playerState = RccEssentials.state.getPlayerState(player.getUuid());
        var homes = playerState.homes;
        var playerContext = PlaceholderContext.of(player);

        var placeholders = Map.of(
                "home", Text.of(name),
                "forceSetButton", Components.button(
                        RccEssentials.CONFIG.textFormats.commands.home.forceSetLabel,
                        RccEssentials.CONFIG.textFormats.commands.home.forceSetHover,
                        "/sethome " + name + " true"
                )
        );

        var exists = homes.containsKey(name);
        if (exists && !forced) {
            var text = Placeholder.parse(
                    RccEssentials.CONFIG.textFormats.commands.home.homeExists,
                    playerContext,
                    placeholders
            );

            context.getSource().sendFeedback(() -> text, false);

            return 1;
        }

        var maxHomes = RccEssentials.CONFIG.homes.maxHomes;
        if(maxHomes >= 0 && homes.size() >= maxHomes && !exists) {
            context.getSource().sendFeedback(() -> Placeholder.parse(
                    RccEssentials.CONFIG.textFormats.commands.home.maxHomesReached,
                    playerContext,
                    placeholders
            ), false);
            return 1;
        }

        var homePosition = new ServerPosition(player);
        homes.put(name, homePosition);

        RccEssentials.state.savePlayerState(player.getUuid(), playerState);

        context.getSource().sendFeedback(() -> Placeholder.parse(
                RccEssentials.CONFIG.textFormats.commands.home.homeSetSuccess,
                playerContext,
                placeholders
        ), false);

        return 1;
    }
}
