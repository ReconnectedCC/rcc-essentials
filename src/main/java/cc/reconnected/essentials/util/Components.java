package cc.reconnected.essentials.util;

import cc.reconnected.essentials.RccEssentials;
import cc.reconnected.library.RccLibrary;
import cc.reconnected.library.text.parser.MarkdownParser;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.PatternPlaceholderParser;
import eu.pb4.placeholders.api.parsers.TextParserV1;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Map;

public class Components {
    public static Text button(Text label, Text hoverText, String command, boolean suggest) {
        var format = suggest ? RccEssentials.CONFIG.textFormats.commands.common.buttonSuggest : RccEssentials.CONFIG.textFormats.commands.common.button;
        var placeholders = Map.of(
                "label", label,
                "hoverText", hoverText,
                "command", Text.of(command)
        );

        format = format.replace("{{command}}", command);
        var text = TextParserUtils.formatText(format);
        return Placeholders.parseText(text, PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN, placeholders);
    }

    public static Text button(String label, String hoverText, String command) {
        var btn = button(
                TextParserUtils.formatText(label),
                TextParserUtils.formatText(hoverText),
                command,
                false
        );

        return btn;
    }

    public static Text buttonSuggest(String label, String hoverText, String command) {
        var btn = button(
                TextParserUtils.formatText(label),
                TextParserUtils.formatText(hoverText),
                command,
                true
        );

        return btn;
    }

    public static MutableText toText(Component component) {
        var json = JSONComponentSerializer.json().serialize(component);
        return Text.Serializer.fromJson(json);
    }

    public static Text chat(SignedMessage message, ServerPlayerEntity player) {
        var luckperms = RccLibrary.getInstance().luckPerms();

        var permissions = luckperms.getPlayerAdapter(ServerPlayerEntity.class).getPermissionData(player);
        var allowAdvancedChatFormat = permissions.checkPermission("rcc.chat.advanced").asBoolean();

        return chat(message.getSignedContent(), allowAdvancedChatFormat);
    }

    public static Text chat(String message, ServerPlayerEntity player) {
        var luckperms = RccLibrary.getInstance().luckPerms();

        var permissions = luckperms.getPlayerAdapter(ServerPlayerEntity.class).getPermissionData(player);
        var allowAdvancedChatFormat = permissions.checkPermission("rcc.chat.advanced").asBoolean();

        return chat(message, allowAdvancedChatFormat);
    }

    public static Text chat(String message, boolean allowAdvancedChatFormat) {
        var enableMarkdown = RccEssentials.CONFIG.chat.enableChatMarkdown;

        for (var repl : RccEssentials.CONFIG.chat.replacements.entrySet()) {
            message = message.replace(repl.getKey(), repl.getValue());
        }

        if (!allowAdvancedChatFormat && !enableMarkdown) {
            return Text.of(message);
        }

        NodeParser parser;
        if (allowAdvancedChatFormat) {
            parser = NodeParser.merge(TextParserV1.DEFAULT, MarkdownParser.defaultParser);
        } else {
            parser = MarkdownParser.defaultParser;
        }

        return parser.parseNode(message).toText();
    }

    public static Text chat(String message, ServerCommandSource source) {
        if (source.isExecutedByPlayer())
            return chat(message, source.getPlayer());
        return chat(message, true);
    }
}
