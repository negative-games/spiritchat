package gg.moonrise.chat.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatTextSanitizerTest {

    @Test
    void stripsMiniMessageTagsButPreservesConfiguredChatItemPlaceholders() {
        String sanitized = ChatTextSanitizer.stripTagsPreservingPlaceholders(
                "<red>Hello <item> and <i></red>",
                List.of("<item>", "<i>")
        );

        assertEquals("Hello <item> and <i>", sanitized);
    }

    @Test
    void sanitizesPlainUserMessagesWithoutRemovingLiteralFormatPlaceholderText() {
        String sanitized = ChatTextSanitizer.sanitizePlainUserMessage(
                "<red>Hello</red> <message> <player> <mention> <item>",
                List.of("<item>", "<i>")
        );

        assertEquals("Hello \\<message> \\<player> \\<mention> <item>", sanitized);
    }

    @Test
    void preservesLegacyColorCodesWithoutAllowingMiniMessageOrFormatInjection() {
        String sanitized = ChatTextSanitizer.sanitizePlainUserMessage(
                "<red>Hello</red> &a<message> <item>",
                List.of("<item>")
        );

        assertEquals("Hello &a\\<message> <item>", sanitized);
    }

    @Test
    void escapesFormatPlaceholdersAfterSanitizingUserText() {
        String escaped = ChatTextSanitizer.escapeUserFormatPlaceholders("literal <message> and <player>");

        assertEquals("literal \\<message> and \\<player>", escaped);
    }

    @Test
    void doesNotDoubleEscapeAlreadyEscapedFormatPlaceholders() {
        String escaped = ChatTextSanitizer.escapeUserFormatPlaceholders("literal \\<message>");

        assertEquals("literal \\<message>", escaped);
    }

    @Test
    void escapesClosingFormatPlaceholdersToo() {
        String escaped = ChatTextSanitizer.escapeUserFormatPlaceholders("</message> </player>");

        assertEquals("\\</message> \\</player>", escaped);
    }

    @Test
    void doesNotTreatUserSuppliedPlaceholderSentinelAsInternalToken() {
        String sentinel = "\uE000SPIRITCHAT_PLACEHOLDER_0\uE001";
        String sanitized = ChatTextSanitizer.stripTagsPreservingPlaceholders(
                sentinel + " <item>",
                List.of("<item>")
        );

        assertEquals(sentinel + " <item>", sanitized);
    }

    @Test
    void skipsUserSuppliedCurrentPlaceholderTokenWhenProtectingPlaceholders() {
        String sentinel = "\uE000SPIRITCHAT_PLACEHOLDER_0_0\uE001";
        String sanitized = ChatTextSanitizer.stripTagsPreservingPlaceholders(
                sentinel + " <item>",
                List.of("<item>")
        );

        assertEquals(sentinel + " <item>", sanitized);
    }

    @Test
    void doesNotTreatUserSuppliedReservedSentinelAsInternalToken() {
        String sentinel = "\uE000SPIRITCHAT_RESERVED_0\uE001";
        String sanitized = ChatTextSanitizer.sanitizePlainUserMessage(
                sentinel + " <item> <message>",
                List.of("<item>")
        );

        assertEquals(sentinel + " <item> \\<message>", sanitized);
    }

    @Test
    void skipsUserSuppliedCurrentReservedTokenWhenProtectingPlaceholders() {
        String sentinel = "\uE000SPIRITCHAT_RESERVED_0_0\uE001";
        String sanitized = ChatTextSanitizer.sanitizePlainUserMessage(
                sentinel + " <item> <message>",
                List.of("<item>")
        );

        assertEquals(sentinel + " <item> \\<message>", sanitized);
    }

    @Test
    void toleratesNullInputs() {
        assertEquals("", ChatTextSanitizer.stripTagsPreservingPlaceholders(null, null));
        assertEquals("", ChatTextSanitizer.sanitizePlainUserMessage(null, null));
        assertEquals("", ChatTextSanitizer.escapeUserFormatPlaceholders(null));
    }
}
