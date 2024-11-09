package cc.reconnected.server.commands.warp;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.struct.ServerPosition;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SetWarpCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var rootCommand = literal("setwarp")
                .requires(Permissions.require("rcc.command.setwarp", 3))
                .then(argument("name", StringArgumentType.word())
                        .executes(context -> execute(context,
                                StringArgumentType.getString(context, "name"))));

        dispatcher.register(rootCommand);
    }

    private static int execute(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrThrow();
        var serverState = RccServer.state.getServerState();

        var warps = serverState.warps;

        var warpPosition = new ServerPosition(player);
        warps.put(name, warpPosition);

        RccServer.state.saveServerState();

        context.getSource().sendFeedback(() -> Text.literal("New warp ")
                .append(Text.literal(name).formatted(Formatting.GOLD))
                .append(" set!")
                .formatted(Formatting.GREEN), false);

        return 1;
    }
}
