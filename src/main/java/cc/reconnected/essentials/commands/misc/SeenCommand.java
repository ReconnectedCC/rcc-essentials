package cc.reconnected.essentials.commands.misc;

import cc.reconnected.essentials.RccEssentials;
import cc.reconnected.essentials.struct.ServerPosition;
import cc.reconnected.library.text.Placeholder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import eu.pb4.placeholders.api.PlaceholderContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SeenCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var rootCommand = dispatcher.register(literal("seen")
                .requires(Permissions.require("rcc.command.seen", true))
                .then(argument("player", StringArgumentType.word())
                        .executes(context -> {
                            var targetName = StringArgumentType.getString(context, "player");
                            var source = context.getSource();
                            source.getServer().getUserCache().findByNameAsync(targetName, (profile) -> {
                                if(profile.isEmpty()) {
                                    source.sendFeedback(() -> Placeholder.parse(RccEssentials.CONFIG.textFormats.commands.seen.playerNotFound), false);
                                    return;
                                }
                                boolean extended;
                                if(context.getSource().isExecutedByPlayer()) {
                                    extended = Permissions.check(context.getSource().getPlayer(), "rcc.command.seen.extended", 2);
                                } else {
                                    extended = true;
                                }

                                var dateFormatter = new SimpleDateFormat(RccEssentials.CONFIG.textFormats.dateTimeFormat);
                                var player = source.getServer().getPlayerManager().getPlayer(profile.get().getId());
                                var playerState = RccEssentials.state.getPlayerState(profile.get().getId());

                                ServerPosition location;
                                if(player == null) {
                                    location = playerState.logoffPosition;
                                } else {
                                    location = new ServerPosition(player);
                                }

                                Map<String, Text> map = Map.of(
                                        "username", Text.of(profile.get().getName()),
                                        "uuid", Text.of(profile.get().getId().toString()),
                                        "firstSeenDate", Text.of(dateFormatter.format(playerState.firstJoinedDate)),
                                        "lastSeenDate", player != null ? Text.of("online") : Text.of(dateFormatter.format(playerState.lastSeenDate)),
                                        "ipAddress", Text.of(playerState.ipAddress),
                                        "location", Text.of(getPositionAsString(location))
                                );

                                var outputString = String.join("\n", RccEssentials.CONFIG.textFormats.commands.seen.base);
                                if(extended) {
                                    outputString += "\n";
                                    outputString += String.join("\n", RccEssentials.CONFIG.textFormats.commands.seen.extended);
                                }

                                final var finalOutput = outputString;
                                if(player != null) {
                                    source.sendFeedback(() -> Placeholder.parse(finalOutput, PlaceholderContext.of(player), map), false);
                                } else {
                                    source.sendFeedback(() -> Placeholder.parse(finalOutput, PlaceholderContext.of(source.getServer()), map), false);
                                }
                            });

                            return 1;
                        })));

        dispatcher.register(literal("playerinfo").redirect(rootCommand));
    }

    public static String getPositionAsString(@Nullable ServerPosition pos) {
        if(pos == null)
            return "Unknown position";

        return String.format("%.01f %.01f %.01f, %s", pos.x, pos.y, pos.z, pos.world);
    }
}
