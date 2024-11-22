package cc.reconnected.essentials.commands.admin;

import cc.reconnected.essentials.RccEssentials;
import cc.reconnected.library.config.ConfigManager;
import cc.reconnected.library.text.Placeholder;
import cc.reconnected.essentials.RccEssentialsConfig;
import cc.reconnected.essentials.api.events.RccEvents;
import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Map;

import static net.minecraft.server.command.CommandManager.literal;

public class RccCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var rootCommand = literal("rcc")
                .requires(Permissions.require("rcc.command.rcc", 3))
                .executes(context -> {
                    var modContainer = FabricLoader.getInstance().getModContainer(RccEssentials.MOD_ID).orElse(null);
                    if(modContainer == null) {
                        context.getSource().sendFeedback(() -> Text.of("Could not find self in mod list???"), false);
                        return 1;
                    }

                    var metadata = modContainer.getMetadata();
                    var placeholders = Map.of(
                            "name", Text.of(metadata.getName()),
                            "version", Text.of(metadata.getVersion().getFriendlyString())
                    );

                    var text = Placeholder.parse(
                            "<gold>${name} v${version}</gold>",
                            placeholders);
                    context.getSource().sendFeedback(() -> text, false);

                    return 1;
                })
                .then(literal("reload")
                        .requires(Permissions.require("rcc.command.rcc.reload", 3))
                        .executes(context -> {
                            context.getSource().sendFeedback(() -> Text.of("Reloading RCC config..."), true);

                            try {
                                RccEssentials.CONFIG = ConfigManager.load(RccEssentialsConfig.class);
                            } catch (Exception e) {
                                RccEssentials.LOGGER.error("Failed to load RCC config", e);
                                context.getSource().sendFeedback(() -> Text.of("Failed to load RCC config. Check console for more info."), true);
                                return 1;
                            }

                            RccEvents.RELOAD.invoker().onReload(RccEssentials.getInstance());

                            context.getSource().sendFeedback(() -> Text.of("Reloaded RCC config"), true);

                            return 1;
                        }));

        dispatcher.register(rootCommand);
    }
}
