package gg.moonrise.chat.config.section.chat;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;

@Getter
@Configuration
public class MentionSettings {

    private static final String DEFAULT_HIGHLIGHT_FORMAT = "<yellow><mention></yellow>";

    @Comment({
            "Enables @PlayerName mentions.",
            "When enabled, messages containing @PlayerName can highlight the mention and notify that player.",
            " ",
            "Default: true"
    })
    private boolean enabled = true;

    @Comment({
            "",
            "Whether mentioned players should receive a ping notification.",
            "This controls the sound and action bar notification only.",
            " ",
            "Default: true"
    })
    private boolean pinging = true;

    @Comment({
            "",
            "Whether players can toggle mention pings with /spiritchat mentions <on|off>.",
            "If disabled, all players use default-player-pinging.",
            " ",
            "Default: true"
    })
    private boolean playerOptions = true;

    @Comment({
            "",
            "The default mention ping preference for players without a saved option.",
            "This is also used for everyone when player-options is disabled.",
            " ",
            "Default: true"
    })
    private boolean defaultPlayerPinging = true;

    @Comment({
            "",
            "Whether @PlayerName should be highlighted in formatted chat.",
            " ",
            "Default: true"
    })
    private boolean highlight = true;

    @Comment({
            "",
            "MiniMessage format used for highlighted mentions.",
            "Placeholders:",
            "  <mention> - The exact mention text from the message.",
            "  <player> - The matched player's current name.",
            " ",
            "Default: \"<yellow><mention></yellow>\""
    })
    private String highlightFormat = DEFAULT_HIGHLIGHT_FORMAT;

    @Comment({
            "",
            "Sound key played for mention pings.",
            "Set this to an empty value to disable the sound while keeping the action bar.",
            " ",
            "Default: minecraft:block.note_block.pling"
    })
    private String sound = "minecraft:block.note_block.pling";

    @Comment({
            "",
            "Mention ping sound volume.",
            "Values are clamped between 0.0 and 10.0.",
            " ",
            "Default: 1.0"
    })
    private double soundVolume = 1.0D;

    @Comment({
            "",
            "Mention ping sound pitch.",
            "Values are clamped between 0.5 and 2.0.",
            " ",
            "Default: 1.2"
    })
    private double soundPitch = 1.2D;

    public String effectiveHighlightFormat() {
        return highlightFormat == null || highlightFormat.isBlank() ? DEFAULT_HIGHLIGHT_FORMAT : highlightFormat;
    }

    public String effectiveSound() {
        return sound == null ? "" : sound;
    }
}
