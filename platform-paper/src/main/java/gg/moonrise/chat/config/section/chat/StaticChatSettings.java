package gg.moonrise.chat.config.section.chat;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;

@Getter
@Configuration
public class StaticChatSettings {

    private static final String DEFAULT_FORMAT = "<player><dark_gray>:</dark_gray> <message>";

    @Comment({
            "Uses one chat format for every player.",
            "Keep this enabled for a simple server-wide format.",
            "Disable this if you want group-chat-settings to choose formats by group.",
            " ",
            "Default: true"
    })
    private boolean enabled = true;

    @Comment({
            "",
            "Format used when static chat is enabled.",
            "Available placeholders:",
            "  <player> - The name of the player sending the message.",
            "  <message> - The message sent by the player.",
            " ",
            "Default: \"<player><dark_gray>:</dark_gray> <message>\""
    })
    private String format = DEFAULT_FORMAT;

    public String effectiveFormat() {
        if (format == null || format.isBlank()) return DEFAULT_FORMAT;

        return format;
    }
}
