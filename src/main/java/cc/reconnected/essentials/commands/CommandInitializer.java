package cc.reconnected.essentials.commands;

import cc.reconnected.essentials.commands.admin.*;
import cc.reconnected.essentials.commands.home.*;
import cc.reconnected.essentials.commands.misc.*;
import cc.reconnected.essentials.commands.spawn.*;
import cc.reconnected.essentials.commands.teleport.*;
import cc.reconnected.essentials.commands.tell.*;
import cc.reconnected.essentials.commands.warp.*;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CommandInitializer {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            RccCommand.register(dispatcher);

            AfkCommand.register(dispatcher);

            TellCommand.register(dispatcher);
            ReplyCommand.register(dispatcher);

            TeleportAskCommand.register(dispatcher);
            TeleportAskHereCommand.register(dispatcher);
            TeleportAcceptCommand.register(dispatcher);
            TeleportDenyCommand.register(dispatcher);
            BackCommand.register(dispatcher);
            TeleportOfflineCommand.register(dispatcher);

            FlyCommand.register(dispatcher);
            GodCommand.register(dispatcher);
            SudoCommand.register(dispatcher);
            BroadcastCommand.register(dispatcher);

            SetSpawnCommand.register(dispatcher);
            DelSpawnCommand.register(dispatcher);
            SpawnCommand.register(dispatcher);

            HomeCommand.register(dispatcher);
            SetHomeCommand.register(dispatcher);
            DeleteHomeCommand.register(dispatcher);
            HomesCommand.register(dispatcher);

            WarpCommand.register(dispatcher);
            SetWarpCommand.register(dispatcher);
            DeleteWarpCommand.register(dispatcher);
            WarpsCommand.register(dispatcher);

            TimeBarCommand.register(dispatcher);
            RestartCommand.register(dispatcher);
            MuteCommand.register(dispatcher);

            NearCommand.register(dispatcher);
            MailCommand.register(dispatcher);
            SeenCommand.register(dispatcher);
            MotdCommand.register(dispatcher);
            SuicideCommand.register(dispatcher);
            SmiteCommand.register(dispatcher);
        });
    }
}
