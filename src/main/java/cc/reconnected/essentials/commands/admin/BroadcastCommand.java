package cc.reconnected.essentials.commands.admin;

import cc.reconnected.essentials.RccEssentials;
import cc.reconnected.library.text.Placeholder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import eu.pb4.placeholders.api.PlaceholderContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BroadcastCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var rootCommand = literal("broadcast")
                .requires(Permissions.require("rcc.command.broadcast", 2))
                .then(argument("message", StringArgumentType.greedyString())
                        .executes(context -> {
                            var message = StringArgumentType.getString(context, "message");
                            var serverContext = PlaceholderContext.of(context.getSource().getServer());

                            RccEssentials.getInstance().broadcast(Placeholder.parse(message, serverContext));

                            return 1;
                        }));

        dispatcher.register(rootCommand);
    }
}
