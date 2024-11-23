package cc.reconnected.essentials.commands.misc;

import cc.reconnected.essentials.RccEssentials;
import cc.reconnected.essentials.core.MailManager;
import cc.reconnected.essentials.struct.PlayerMail;
import cc.reconnected.essentials.util.Components;
import cc.reconnected.library.text.Placeholder;
import cc.reconnected.library.text.parser.MarkdownParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.placeholders.api.PlaceholderContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.text.SimpleDateFormat;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.*;

public class MailCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var rootCommand = literal("mail")
                .requires(Permissions.require("rcc.command.mail", true))
                .executes(MailCommand::listMails)
                .then(literal("send")
                        .then(argument("recipient", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    var playerManager = context.getSource().getServer().getPlayerManager();
                                    return CommandSource.suggestMatching(
                                            playerManager.getPlayerNames(),
                                            builder);
                                })
                                .then(argument("message", StringArgumentType.greedyString())
                                        .executes(MailCommand::sendMail)
                                )
                        )
                )
                .then(literal("read")
                        .then(argument("index", IntegerArgumentType.integer(0))
                                .executes(MailCommand::readMail)))
                .then(literal("delete")
                        .then(argument("index", IntegerArgumentType.integer(0))
                                .executes(MailCommand::deleteMail)));

        dispatcher.register(rootCommand);
    }

    private static int listMails(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrThrow();
        var playerContext = PlaceholderContext.of(player);
        var mails = MailManager.getMailList(player.getUuid());
        var serverState = RccEssentials.state.getServerState();

        var output = Text.empty()
                .append(Placeholder.parse(RccEssentials.CONFIG.textFormats.commands.mail.mailListHeader, playerContext))
                .append(Text.of("\n"));

        for (var i = 0; i < mails.size(); i++) {
            if (i > 0)
                output = output.append(Text.of("\n"));

            var mail = mails.get(i);
            var index = i + 1;

            var readButton = Components.button(
                    RccEssentials.CONFIG.textFormats.commands.mail.readButton,
                    RccEssentials.CONFIG.textFormats.commands.mail.hoverRead,
                    "/mail read " + index
            );

            var senderName = serverState.usernameCache.getOrDefault(mail.sender, mail.sender.toString());
            var dateFormatter = new SimpleDateFormat(RccEssentials.CONFIG.textFormats.dateTimeFormat);
            var placeholders = Map.of(
                    "index", Text.of(String.valueOf(index)),
                    "sender", Text.of(senderName),
                    "date", Text.of(dateFormatter.format(mail.date)),
                    "readButton", readButton
            );
            output = output.append(Placeholder.parse(RccEssentials.CONFIG.textFormats.commands.mail.mailListEntry, playerContext, placeholders));
        }

        final var finalOutput = output;

        context.getSource().sendFeedback(() -> finalOutput, false);

        return 1;
    }

    private static int readMail(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrThrow();
        var playerContext = PlaceholderContext.of(player);
        var mails = MailManager.getMailList(player.getUuid());
        var serverState = RccEssentials.state.getServerState();
        var index = IntegerArgumentType.getInteger(context, "index") - 1;

        if (index < 0 || index >= mails.size()) {
            context.getSource().sendFeedback(() -> Placeholder.parse(RccEssentials.CONFIG.textFormats.commands.mail.notFound), false);
            return 1;
        }

        var mail = mails.get(index);

        var username = serverState.usernameCache.getOrDefault(mail.sender, mail.sender.toString());

        var replyButton = Components.buttonSuggest(
                RccEssentials.CONFIG.textFormats.commands.mail.replyButton,
                RccEssentials.CONFIG.textFormats.commands.mail.hoverReply,
                "/mail send " + username + " "
        );
        var deleteButton = Components.button(
                RccEssentials.CONFIG.textFormats.commands.mail.deleteButton,
                RccEssentials.CONFIG.textFormats.commands.mail.hoverDelete,
                "/mail delete " + index
        );

        var senderName = serverState.usernameCache.getOrDefault(mail.sender, mail.sender.toString());
        var dateFormatter = new SimpleDateFormat(RccEssentials.CONFIG.textFormats.dateTimeFormat);
        var message = MarkdownParser.defaultParser.parseNode(mail.message);
        var placeholders = Map.of(
                "index", Text.of(String.valueOf(index)),
                "sender", Text.of(senderName),
                "date", Text.of(dateFormatter.format(mail.date)),
                "message", message.toText(),
                "replyButton", replyButton,
                "deleteButton", deleteButton
        );

        context.getSource().sendFeedback(() -> Placeholder.parse(RccEssentials.CONFIG.textFormats.commands.mail.mailDetails, playerContext, placeholders), false);

        return 1;
    }

    private static int deleteMail(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrThrow();
        var playerContext = PlaceholderContext.of(player);
        var index = IntegerArgumentType.getInteger(context, "index") - 1;

        if (MailManager.deleteMail(player.getUuid(), index)) {
            context.getSource().sendFeedback(() -> Placeholder.parse(RccEssentials.CONFIG.textFormats.commands.mail.mailDeleted, playerContext), false);
        } else {
            context.getSource().sendFeedback(() -> Placeholder.parse(RccEssentials.CONFIG.textFormats.commands.mail.notFound), false);
        }

        return 1;
    }

    private static int sendMail(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var sender = context.getSource().getPlayerOrThrow();
        var username = StringArgumentType.getString(context, "recipient");
        context.getSource().getServer().getUserCache().findByNameAsync(username, gameProfile -> {
            if (gameProfile.isEmpty()) {
                var playerContext = PlaceholderContext.of(sender);

                var placeholders = Map.of(
                        "recipient", Text.of(username)
                );

                context.getSource().sendFeedback(() -> Placeholder.parse(RccEssentials.CONFIG.textFormats.commands.mail.playerNotFound, playerContext, placeholders), false);
                return;
            }

            var message = StringArgumentType.getString(context, "message");
            var recipient = gameProfile.get();
            var server = context.getSource().getServer();

            var mail = new PlayerMail(message, sender.getUuid());
            MailManager.sendMail(recipient.getId(), mail);

            var senderContext = PlaceholderContext.of(sender);

            context.getSource().sendFeedback(() -> Placeholder.parse(RccEssentials.CONFIG.textFormats.commands.mail.mailSent, senderContext), false);

            var recPlayer = server.getPlayerManager().getPlayer(recipient.getId());
            if (recPlayer == null) {
                return;
            }

            var recContext = PlaceholderContext.of(recPlayer);
            recPlayer.sendMessage(Placeholder.parse(RccEssentials.CONFIG.textFormats.commands.mail.mailReceived, recContext));
        });

        return 1;
    }
}
