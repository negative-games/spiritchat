package gg.moonrise.chat.config.section.chat;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.Material;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatItemSettingsTest {

    @Test
    void detectsConfiguredChatItemPlaceholders() {
        ChatItemSettings settings = new ChatItemSettings();

        assertTrue(settings.containsChatItemSyntax("look at <item>"));
        assertTrue(settings.containsChatItemSyntax("look at <i>"));
    }

    @Test
    void ignoresEscapedChatItemPlaceholders() {
        ChatItemSettings settings = new ChatItemSettings();

        assertFalse(settings.containsChatItemSyntax("literal \\<item>"));
        assertFalse(settings.containsChatItemSyntax("literal \\<i>"));
    }

    @Test
    void treatsEvenBackslashesBeforePlaceholdersAsUnescaped() {
        ChatItemSettings settings = new ChatItemSettings();

        assertTrue(settings.containsChatItemSyntax("escaped slash \\\\<item>"));
    }

    @Test
    void toleratesNullPlaceholderListFromMalformedConfig() throws Exception {
        ChatItemSettings settings = new ChatItemSettings();
        setField(settings, "placeholders", null);

        assertEquals(List.of("<i>", "<item>"), settings.effectivePlaceholders());
        assertTrue(settings.containsChatItemSyntax("look at <item>"));
    }

    @Test
    void filtersInvalidPlaceholderEntriesFromMalformedConfig() throws Exception {
        ChatItemSettings settings = new ChatItemSettings();
        setField(settings, "placeholders", List.of("<item>", "", "<item>", "<held>"));

        assertEquals(List.of("<item>", "<held>"), settings.effectivePlaceholders());
    }

    @Test
    void filtersReservedFormatPlaceholdersFromChatItemPlaceholders() throws Exception {
        ChatItemSettings settings = new ChatItemSettings();
        setField(settings, "placeholders", List.of("<item>", "<message>", "<PLAYER>", "</mention>"));

        assertEquals(List.of("<item>"), settings.effectivePlaceholders());
    }

    @Test
    void fallsBackToDefaultPermissionWhenMalformedConfigProvidesBlankPermission() throws Exception {
        ChatItemSettings settings = new ChatItemSettings();

        setField(settings, "permission", null);
        assertEquals("spiritchat.chatitem", settings.effectivePermission());

        setField(settings, "permission", " ");
        assertEquals("spiritchat.chatitem", settings.effectivePermission());
    }

    @Test
    void toleratesNullBlockedItemListFromMalformedConfig() throws Exception {
        ChatItemSettings settings = new ChatItemSettings();
        setField(settings, "blockedItemTypes", null);

        assertTrue(settings.isItemTypeBlocked(Material.AIR));
        assertTrue(settings.isItemTypeBlocked(Material.CAVE_AIR));
        assertTrue(settings.isItemTypeBlocked(Material.VOID_AIR));
        assertFalse(settings.isItemTypeBlocked(Material.DIAMOND));
    }

    @Test
    void filtersInvalidBlockedItemEntriesFromMalformedConfig() throws Exception {
        ChatItemSettings settings = new ChatItemSettings();
        setField(settings, "blockedItemTypes", java.util.Arrays.asList(Material.AIR, null, Material.AIR, Material.STONE));

        assertEquals(List.of(Material.AIR, Material.STONE), settings.effectiveBlockedItemTypes());
    }

    private void setField(ChatItemSettings settings, String name, Object value) throws Exception {
        Field field = ChatItemSettings.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(settings, value);
    }
}
