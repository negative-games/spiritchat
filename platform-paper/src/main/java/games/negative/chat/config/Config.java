package games.negative.chat.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import games.negative.chat.config.section.chat.ChatItemSettings;
import games.negative.chat.config.section.chat.StaticChatSettings;
import games.negative.chat.config.section.database.DatabaseSettings;
import games.negative.chat.config.section.display.PlayerDisplaySettings;
import games.negative.chat.config.section.log.LoggingSettings;
import lombok.Getter;

@Getter
@Configuration
public class Config {

    @Comment({
            "Whether or not to check for updates."
    })
    private boolean checkForUpdates = true;

    @Comment({
            "",
            "Settings for player display"
    })
    private PlayerDisplaySettings playerDisplaySettings = new PlayerDisplaySettings();

    @Comment({
            "",
            "Settings for chat item formatting"
    })
    private ChatItemSettings chatItemSettings = new ChatItemSettings();

    @Comment({
            "",
            "Settings for static chat formatting"
    })
    private StaticChatSettings staticChatSettings = new StaticChatSettings();

    @Comment({
            "",
            "Chat Logging settings"
    })
    private LoggingSettings loggingSettings = new LoggingSettings();

    @Comment({
            "",
            "Database connection settings"
    })
    private DatabaseSettings databaseSettings = new DatabaseSettings();
}
