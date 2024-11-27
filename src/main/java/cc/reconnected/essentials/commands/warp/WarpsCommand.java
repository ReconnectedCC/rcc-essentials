package cc.reconnected.essentials.commands.warp;

import cc.reconnected.essentials.RccEssentials;
import cc.reconnected.library.text.Placeholder;
import com.mojang.brigadier.CommandDispatcher;
import eu.pb4.placeholders.api.PlaceholderContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Map;

import static net.minecraft.server.command.CommandManager.literal;

public class WarpsCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var rootCommand = literal("warps")
                .requires(Permissions.require("rcc.command.warps", true))
                .executes(context -> {
                    var player = context.getSource().getPlayerOrThrow();
                    var serverState = RccEssentials.state.getServerState();
                    var warpList = serverState.warps.keySet().stream().toList();
                    var playerContext = PlaceholderContext.of(player);

                    if(warpList.isEmpty()) {
                        context.getSource().sendFeedback(() -> Placeholder.parse(
                                RccEssentials.CONFIG.textFormats.commands.warp.noWarps,
                                playerContext
                        ), false);
                        return 1;
                    }

                    var listText = Text.empty();
                    var comma = Placeholder.parse(RccEssentials.CONFIG.textFormats.commands.warp.warpsComma);
                    for(var i = 0; i < warpList.size(); i++) {
                        if (i > 0) {
                            listText = listText.append(comma);
                        }
                        var placeholders = Map.of(
                                "warp", Text.of(warpList.get(i))
                        );

                        listText = listText.append(Placeholder.parse(
                                RccEssentials.CONFIG.textFormats.commands.warp.warpsFormat,
                                playerContext,
                                placeholders
                        ));
                    }

                    var placeholders = Map.of(
                            "warpList", (Text) listText
                    );
                    context.getSource().sendFeedback(() -> Placeholder.parse(
                            RccEssentials.CONFIG.textFormats.commands.warp.warpList,
                            playerContext,
                            placeholders
                    ), false);

                    return 1;
                });

        dispatcher.register(rootCommand);
    }
}