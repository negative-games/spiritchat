package gg.moonrise.chat.chat.format;

import gg.moonrise.engine.message.util.MiniMessageUtil;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatMessageFormatterTest {

    @Test
    void preparesPlainMessageByStrippingMiniMessageAndEscapingFormatPlaceholders() {
        FormattedChatMessage message = ChatMessageFormatter.prepare(
                "<red>Hello</red> <message> <item>",
                List.of("<item>"),
                false
        );

        assertEquals("Hello \\<message> <item>", message.input());
        assertEquals("Hello \\<message> <item>", MiniMessageUtil.componentToPlainText(message.component()));
    }

    @Test
    void preparesLegacyColoredMessageWithoutAllowingMiniMessageInjection() {
        FormattedChatMessage message = ChatMessageFormatter.prepare(
                "&aHello <red>bad</red> <message>",
                List.of(),
                true
        );

        assertEquals("&aHello bad \\<message>", message.input());
        assertEquals("Hello bad \\<message>", MiniMessageUtil.componentToPlainText(message.component()));
    }

    @Test
    void preservesConfiguredChatItemPlaceholdersForLaterReplacement() {
        FormattedChatMessage message = ChatMessageFormatter.prepare(
                "<i> <item> <player>",
                List.of("<i>", "<item>"),
                false
        );

        assertEquals("<i> <item> \\<player>", message.input());
        assertEquals("<i> <item> \\<player>", MiniMessageUtil.componentToPlainText(message.component()));
    }

    @Test
    void preservesConfiguredChatItemPlaceholdersWhenLegacyColorsAreEnabled() {
        FormattedChatMessage message = ChatMessageFormatter.prepare(
                "&aLook <item>",
                List.of("<item>"),
                true
        );

        assertEquals("&aLook <item>", message.input());
        assertEquals("Look <item>", MiniMessageUtil.componentToPlainText(message.component()));
    }

    @Test
    void keepsEscapedChatItemPlaceholdersEscapedForLaterReplacementCheck() {
        FormattedChatMessage message = ChatMessageFormatter.prepare(
                "literal \\<item>",
                List.of("<item>"),
                false
        );

        assertEquals("literal \\<item>", message.input());
        assertEquals("literal \\<item>", MiniMessageUtil.componentToPlainText(message.component()));
    }
}
