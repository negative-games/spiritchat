package gg.moonrise.chat.config.section.chat;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;

@Getter
@Configuration
public class StaticChatSettings {

    @Comment({
            "Whether or not to use static chat formatting.",
            "If enabled, all players will see the same chat format regardless of permissions or other factors",
            "This is useful for servers that want a consistent chat experience for all players.",
            " ",
            "Default: true"
    })
    private boolean enabled = true;

    @Comment({
            "",
            "The static chat format to use if static chat is enabled.",
            "You can use the following placeholders:",
            "  {player} - The name of the player sending the message.",
            "  {message} - The message sent by the player.",
            " ",
            "Default: \"{player}&8:&r {message}\""
    })
    private String format = "{player}&8:&r {message}";
}
