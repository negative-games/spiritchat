package gg.moonrise.chat.config.section.chat;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;
import org.bukkit.Material;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Getter
@Configuration
public class ChatItemSettings {

    private static final String DEFAULT_PERMISSION = "spiritchat.chatitem";
    private static final List<String> DEFAULT_PLACEHOLDERS = List.of("<i>", "<item>");
    private static final List<Material> DEFAULT_BLOCKED_ITEM_TYPES = List.of(
            Material.AIR,
            Material.CAVE_AIR,
            Material.VOID_AIR
    );
    private static final List<String> RESERVED_PLACEHOLDERS = List.of(
            "<message>",
            "</message>",
            "<player>",
            "</player>",
            "<mention>",
            "</mention>"
    );

    @Comment({
            "Allows players to show their held item in chat.",
            "Players still need the permission below.",
            " ",
            "Default: true"
    })
    private boolean enabled = true;

    @Comment({
            "",
            "The permission required to use chat item formatting.",
            "Players without this permission will not have their held item displayed in chat messages.",
            " ",
            "Default: spiritchat.chatitem"
    })
    private String permission = DEFAULT_PERMISSION;

    @Comment({
            "",
            "Text players can type to show their held item in chat.",
            "Escaped placeholders, such as \\<item>, are left as normal text.",
            " ",
            "Default: [\"<i>\", \"<item>\"]"
    })
    private List<String> placeholders = DEFAULT_PLACEHOLDERS;

    @Comment({
            "",
            "Item types that cannot be shown in chat.",
            "AIR, CAVE_AIR, and VOID_AIR prevent empty-hand showcases.",
            " ",
            "Default: [AIR, CAVE_AIR, VOID_AIR]"
    })
    private List<Material> blockedItemTypes = DEFAULT_BLOCKED_ITEM_TYPES;

    public boolean isItemTypeBlocked(Material material) {
        return material != null && effectiveBlockedItemTypes().contains(material);
    }

    public String effectivePermission() {
        if (permission == null || permission.isBlank()) return DEFAULT_PERMISSION;

        return permission;
    }

    public boolean containsChatItemSyntax(String input) {
        if (input == null || input.isEmpty()) return false;

        for (String placeholder : effectivePlaceholders()) {
            if (containsUnescaped(input, placeholder)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsUnescaped(String input, String placeholder) {
        int index = input.indexOf(placeholder);
        while (index >= 0) {
            if (!isEscaped(input, index)) return true;

            index = input.indexOf(placeholder, index + placeholder.length());
        }
        return false;
    }

    private boolean isEscaped(String input, int index) {
        int backslashes = 0;
        for (int cursor = index - 1; cursor >= 0 && input.charAt(cursor) == '\\'; cursor--) {
            backslashes++;
        }
        return backslashes % 2 == 1;
    }

    public List<String> effectivePlaceholders() {
        if (placeholders == null) return DEFAULT_PLACEHOLDERS;

        return placeholders.stream()
                .filter(Objects::nonNull)
                .filter(placeholder -> !placeholder.isEmpty())
                .filter(placeholder -> !RESERVED_PLACEHOLDERS.contains(placeholder.toLowerCase(Locale.ROOT)))
                .distinct()
                .toList();
    }

    public List<Material> effectiveBlockedItemTypes() {
        if (blockedItemTypes == null) return DEFAULT_BLOCKED_ITEM_TYPES;

        return blockedItemTypes.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}
