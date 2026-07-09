package gg.moonrise.chat.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import gg.moonrise.chat.config.section.chat.AntiMessageSigningSettings;
import gg.moonrise.chat.config.section.chat.ChatItemSettings;
import gg.moonrise.chat.config.section.chat.GroupChatSettings;
import gg.moonrise.chat.config.section.chat.StaticChatSettings;
import gg.moonrise.chat.config.section.database.DatabaseSettings;
import gg.moonrise.chat.config.section.display.PlayerDisplaySettings;
import gg.moonrise.chat.config.section.log.LoggingSettings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Config {

    private final General general;
    private final PlayerDisplaySettings playerDisplaySettings;
    private final LoggingSettings loggingSettings;
    private final ChatItemSettings chatItemSettings;
    private final AntiMessageSigningSettings antiMessageSigningSettings;
    private final StaticChatSettings staticChatSettings;
    private final GroupChatSettings groupChatSettings;
    private final DatabaseSettings databaseSettings;

    public boolean isCheckForUpdates() {
        return general.isCheckForUpdates();
    }

    public static Config compose(General general, Chat chat, Database database) {
        return new Config(
                general,
                general.getPlayerDisplaySettings(),
                general.getLoggingSettings(),
                chat.getChatItemSettings(),
                chat.getAntiMessageSigningSettings(),
                chat.getStaticChatSettings(),
                chat.getGroupChatSettings(),
                database.getDatabaseSettings()
        );
    }

    @Getter
    @Configuration
    public static class General {

        @Comment({
                "Whether or not to check for updates.",
                " ",
                "Default: true"
        })
        private boolean checkForUpdates = true;

        @Comment({
                "",
                "Settings for player display"
        })
        private PlayerDisplaySettings playerDisplaySettings = new PlayerDisplaySettings();

        @Comment({
                "",
                "Chat logging settings"
        })
        private LoggingSettings loggingSettings = new LoggingSettings();
    }

    @Getter
    @Configuration
    public static class Chat {

        @Comment({
                "Settings for chat item formatting"
        })
        private ChatItemSettings chatItemSettings = new ChatItemSettings();

        @Comment({
                "",
                "Settings for anti-message-signing chat packet rewriting"
        })
        private AntiMessageSigningSettings antiMessageSigningSettings = new AntiMessageSigningSettings();

        @Comment({
                "",
                "Settings for static chat formatting"
        })
        private StaticChatSettings staticChatSettings = new StaticChatSettings();

        @Comment({
                "",
                "Settings for group chat formatting"
        })
        private GroupChatSettings groupChatSettings = new GroupChatSettings();
    }

    @Getter
    @Configuration
    public static class Database {

        @Comment({
                "Database connection settings"
        })
        private DatabaseSettings databaseSettings = new DatabaseSettings();
    }
}
