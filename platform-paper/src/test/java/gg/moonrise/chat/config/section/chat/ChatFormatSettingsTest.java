package gg.moonrise.chat.config.section.chat;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatFormatSettingsTest {

    @Test
    void staticDefaultFormatUsesMiniMessageSyntax() {
        StaticChatSettings settings = new StaticChatSettings();

        assertFalse(settings.getFormat().contains("&"));
    }

    @Test
    void staticFormatFallsBackWhenMalformedConfigProvidesBlankValue() throws Exception {
        StaticChatSettings settings = new StaticChatSettings();
        Field field = StaticChatSettings.class.getDeclaredField("format");
        field.setAccessible(true);

        field.set(settings, null);
        assertEquals("<player><dark_gray>:</dark_gray> <message>", settings.effectiveFormat());

        field.set(settings, " ");
        assertEquals("<player><dark_gray>:</dark_gray> <message>", settings.effectiveFormat());
    }

    @Test
    void groupDefaultFormatsUseMiniMessageSyntax() {
        GroupChatSettings settings = new GroupChatSettings();

        settings.getFormats().values().forEach(format -> assertFalse(format.contains("&")));
    }

    @Test
    void groupFormatLookupIsCaseInsensitive() {
        GroupChatSettings settings = new GroupChatSettings();

        assertEquals(
                "<dark_red>[Admin]</dark_red> <player><dark_gray>:</dark_gray> <message>",
                settings.firstFormat(List.of("ADMIN")).get()
        );
    }

    @Test
    void firstGroupFormatUsesFirstMatchingWeightedGroup() {
        GroupChatSettings settings = new GroupChatSettings();

        assertEquals(
                "<dark_red>[Admin]</dark_red> <player><dark_gray>:</dark_gray> <message>",
                settings.firstFormat(List.of("missing", "ADMIN", "default")).get()
        );
    }

    @Test
    void groupFormatsTolerateMalformedConfigValues() throws Exception {
        GroupChatSettings settings = new GroupChatSettings();
        Map<String, String> formats = new HashMap<>();
        formats.put("Admin", "<red><player>: <message>");
        formats.put("empty", "");
        formats.put(null, "<player>");
        formats.put("missing", null);
        setFormats(settings, formats);

        assertEquals("<red><player>: <message>", settings.firstFormat(List.of("admin")).get());
        assertTrue(settings.firstFormat(List.of("empty")).isEmpty());
        assertTrue(settings.firstFormat(List.of("missing")).isEmpty());
    }

    @Test
    void groupFormatsTolerateNullMapFromMalformedConfig() throws Exception {
        GroupChatSettings settings = new GroupChatSettings();
        setFormats(settings, null);

        assertTrue(settings.effectiveFormats().isEmpty());
        assertTrue(settings.firstFormat(List.of("default")).isEmpty());
    }

    private void setFormats(GroupChatSettings settings, Map<String, String> formats) throws Exception {
        Field field = GroupChatSettings.class.getDeclaredField("formats");
        field.setAccessible(true);
        field.set(settings, formats);
    }
}
