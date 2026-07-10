package gg.moonrise.chat.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import gg.moonrise.chat.config.section.chat.AntiMessageSigningSettings;
import gg.moonrise.chat.config.section.chat.ChatItemSettings;
import gg.moonrise.chat.config.section.chat.GroupChatSettings;
import gg.moonrise.chat.config.section.chat.MentionSettings;
import gg.moonrise.chat.config.section.chat.StaticChatSettings;
import gg.moonrise.chat.config.section.database.DatabaseSettings;
import gg.moonrise.chat.config.section.log.LoggingSettings;
import lombok.Getter;

@Getter
@Configuration
public class Config {

    @Comment({
            "General plugin behavior settings."
    })
    private General general = new General();

    @Comment({
            "",
            "Chat formatting, mentions, chat item, and chat-signing compatibility settings."
    })
    private Chat chat = new Chat();

    @Comment({
            "",
            "Persistent storage settings.",
            "SpiritChat uses this storage for player options and chat logs.",
            "SQLite is the default and stores data in the plugin folder."
    })
    private Storage storage = new Storage();

    public LoggingSettings getLoggingSettings() {
        return general.getLoggingSettings();
    }

    public ChatItemSettings getChatItemSettings() {
        return chat.getChatItemSettings();
    }

    public MentionSettings getMentionSettings() {
        return chat.getMentionSettings();
    }

    public AntiMessageSigningSettings getAntiMessageSigningSettings() {
        return chat.getAntiMessageSigningSettings();
    }

    public StaticChatSettings getStaticChatSettings() {
        return chat.getStaticChatSettings();
    }

    public GroupChatSettings getGroupChatSettings() {
        return chat.getGroupChatSettings();
    }

    public DatabaseSettings getDatabaseSettings() {
        return storage.getDatabaseSettings();
    }

    @Getter
    @Configuration
    public static class General {

        @Comment({
                "Chat log storage settings.",
                "When enabled, messages are written to the configured database."
        })
        private LoggingSettings loggingSettings = new LoggingSettings();
    }

    @Getter
    @Configuration
    public static class Chat {

        @Comment({
                "Chat item formatting settings.",
                "Allows players to show their held item in chat with configured placeholders."
        })
        private ChatItemSettings chatItemSettings = new ChatItemSettings();

        @Comment({
                "",
                "Player mention settings.",
                "Controls @PlayerName highlighting and ping notifications."
        })
        private MentionSettings mentionSettings = new MentionSettings();

        @Comment({
                "",
                "Chat-signing compatibility settings.",
                "Keeps SpiritChat formatted messages compatible with modern clients and report-disabling client mods."
        })
        private AntiMessageSigningSettings antiMessageSigningSettings = new AntiMessageSigningSettings();

        @Comment({
                "",
                "Static chat formatting settings.",
                "Used when every player should share the same chat format."
        })
        private StaticChatSettings staticChatSettings = new StaticChatSettings();

        @Comment({
                "",
                "Group chat formatting settings.",
                "Used when chat formats should be selected from the player's LuckPerms group."
        })
        private GroupChatSettings groupChatSettings = new GroupChatSettings();
    }

    @Getter
    @Configuration
    public static class Storage {

        @Comment({
                "Database connection settings.",
                "Default SQLite creates a local storage.db file.",
                "External SQL databases can be configured here when needed."
        })
        private DatabaseSettings databaseSettings = new DatabaseSettings();
    }
}
