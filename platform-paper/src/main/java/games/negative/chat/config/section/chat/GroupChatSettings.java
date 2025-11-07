package games.negative.chat.config.section.chat;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import games.negative.alumina.message.Message;
import io.vavr.control.Option;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Getter
@Configuration
public class GroupChatSettings {

    @Comment({
            "",
            "Whether or not to use group chat formatting.",
            "If enabled, chat format will be determined by the player's group.",
            "This is useful for servers that want to differentiate chat formats based on player groups.",
            "",
            "Default: false"
    })
    private boolean enabled = false;

    @Comment({
            "",
            "The group chat format to use if group chat is enabled.",
            "You can use the following placeholders:",
            "  {player} - The name of the player sending the message.",
            "  {message} - The message sent by the player.",
            " ",
            "Default: {","\"default\": \"{player}&8:&r {message}\", \"admin\": \"&4[Admin] {player}&8:&r {message}\"","}"
    })
    private Map<String, String> formats = Map.of(
            "default", "{player}&8:&r {message}",
            "admin", "&4[Admin] {player}&8:&r {message}"
    );

    public Option<Message> format(String group) {
        return Option.of(group).filter(Objects::nonNull).map(Message::of);
    }
}
