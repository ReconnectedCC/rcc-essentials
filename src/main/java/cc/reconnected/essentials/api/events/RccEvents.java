package cc.reconnected.essentials.api.events;

import cc.reconnected.essentials.RccEssentials;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.luckperms.api.LuckPerms;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class RccEvents {
    public static final Event<Reload> RELOAD = EventFactory.createArrayBacked(Reload.class, callbacks ->
            (instance) -> {
                for (Reload callback : callbacks) {
                    callback.onReload(instance);
                }
            });

    public static final Event<Welcome> WELCOME = EventFactory.createArrayBacked(Welcome.class, callbacks ->
            (player, server) -> {
                for (Welcome callback : callbacks) {
                    callback.onWelcome(player, server);
                }
            });

    public static final Event<UsernameChange> USERNAME_CHANGE = EventFactory.createArrayBacked(UsernameChange.class, callbacks ->
            (player, previousUsername) -> {
                for (UsernameChange callback : callbacks) {
                    callback.onUsernameChange(player, previousUsername);
                }
            });

    public static final Event<PlayerCommand> PLAYER_COMMAND = EventFactory.createArrayBacked(PlayerCommand.class, callbacks ->
            (player, command) -> {
                for (PlayerCommand callback : callbacks) {
                    callback.onPlayerCommand(player, command);
                }
            });

    @FunctionalInterface
    public interface Reload {
        void onReload(RccEssentials instance);
    }

    @FunctionalInterface
    public interface Welcome {
        void onWelcome(ServerPlayerEntity player, MinecraftServer server);
    }

    @FunctionalInterface
    public interface UsernameChange {
        void onUsernameChange(ServerPlayerEntity player, String previousUsername);
    }

    @FunctionalInterface
    public interface PlayerCommand {
        void onPlayerCommand(ServerPlayerEntity player, String command);
    }
}
