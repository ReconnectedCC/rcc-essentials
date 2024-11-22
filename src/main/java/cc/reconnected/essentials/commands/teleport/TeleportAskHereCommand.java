package cc.reconnected.essentials.commands.teleport;

import cc.reconnected.essentials.RccEssentials;
import cc.reconnected.library.text.Placeholder;
import cc.reconnected.essentials.core.TeleportTracker;
import cc.reconnected.essentials.util.Components;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.placeholders.api.PlaceholderContext;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TeleportAskHereCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var node = dispatcher.register(literal("tpahere")
                .then(argument("player", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            var playerManager = context.getSource().getServer().getPlayerManager();
                            return CommandSource.suggestMatching(
                                    playerManager.getPlayerNames(),
                                    builder);
                        })
                        .executes(context -> {
                            execute(context);
                            return 1;
                        })));

        dispatcher.register(literal("tpaskhere").redirect(node));
    }

    private static void execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();
        var player = context.getSource().getPlayerOrThrow();
        var server = source.getServer();
        var targetName = StringArgumentType.getString(context, "player");
        var playerManager = server.getPlayerManager();
        var target = playerManager.getPlayer(targetName);
        var playerContext = PlaceholderContext.of(player);
        if (target == null) {
            var placeholders = Map.of(
                    "targetPlayer", Text.of(targetName)
            );
            source.sendFeedback(() -> Placeholder.parse(
                    RccEssentials.CONFIG.textFormats.commands.teleportRequest.playerNotFound,
                    playerContext,
                    placeholders
            ), false);
            return;
        }

        var request = new TeleportTracker.TeleportRequest(target.getUuid(), player.getUuid());
        var targetRequests = TeleportTracker.teleportRequests.get(target.getUuid());
        targetRequests.addLast(request);

        var targetContext = PlaceholderContext.of(target);
        var placeholders = Map.of(
                "requesterPlayer", player.getDisplayName(),
                "acceptButton", Components.button(
                        RccEssentials.CONFIG.textFormats.commands.common.accept,
                        RccEssentials.CONFIG.textFormats.commands.teleportRequest.hoverAccept,
                        "/tpaccept " + request.requestId),
                "refuseButton", Components.button(
                        RccEssentials.CONFIG.textFormats.commands.common.refuse,
                        RccEssentials.CONFIG.textFormats.commands.teleportRequest.hoverRefuse,
                        "/tpdeny " + request.requestId)
        );

        target.sendMessage(Placeholder.parse(
                RccEssentials.CONFIG.textFormats.commands.teleportRequest.pendingTeleportHere,
                targetContext,
                placeholders
        ));

        source.sendFeedback(() -> Placeholder.parse(
                RccEssentials.CONFIG.textFormats.commands.teleportRequest.requestSent,
                playerContext
        ), false);
    }
}
