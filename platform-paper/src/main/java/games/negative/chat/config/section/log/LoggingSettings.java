package games.negative.chat.config.section.log;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;

@Getter
@Configuration
public class LoggingSettings {

    @Comment({
            "",
            "Whether or not to enable tracking chat logs",
            "This will log all chat messages sent by players to a database",
            "to be retrieved later for moderation purposes.",
            " ",
            "Changes here require a server restart to take effect.",
            " ",
            "Default: false"
    })
    private boolean enabled = false;


}
