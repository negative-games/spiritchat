package gg.moonrise.chat.config.section.chat;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import io.vavr.control.Option;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Getter
@Configuration
public class GroupChatSettings {

    @Comment({
            "Uses different chat formats for different permission groups.",
            "Requires LuckPerms.",
            "Static chat takes priority when static-chat-settings.enabled is true.",
            " ",
            "Default: false"
    })
    private boolean enabled = false;

    @Comment({
            "",
            "Formats used when group chat is enabled.",
            "Keys are group names. The highest-weight matching group is used.",
            "Available placeholders:",
            "  <player> - The name of the player sending the message.",
            "  <message> - The message sent by the player.",
            " ",
            "Default:",
            " default: \"<player><dark_gray>:</dark_gray> <message>\"",
            " admin: \"<dark_red>[Admin]</dark_red> <player><dark_gray>:</dark_gray> <message>\""
    })
    private Map<String, String> formats = Map.of(
            "default", "<player><dark_gray>:</dark_gray> <message>",
            "admin", "<dark_red>[Admin]</dark_red> <player><dark_gray>:</dark_gray> <message>"
    );

    public Option<String> format(String group) {
        if (group == null || group.isBlank()) return Option.none();

        return Option.of(effectiveFormats().get(normalize(group)))
                .filter(format -> !format.isBlank());
    }

    public Map<String, String> effectiveFormats() {
        if (formats == null || formats.isEmpty()) return Map.of();

        Map<String, String> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : formats.entrySet()) {
            String group = entry.getKey();
            String format = entry.getValue();
            if (group == null || group.isBlank() || format == null || format.isBlank()) continue;

            normalized.putIfAbsent(normalize(group), format);
        }
        return Map.copyOf(normalized);
    }

    private String normalize(String group) {
        return group.toLowerCase(Locale.ROOT);
    }
}
