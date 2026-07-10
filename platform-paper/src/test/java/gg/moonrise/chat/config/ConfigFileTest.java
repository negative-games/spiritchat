package gg.moonrise.chat.config;

import de.exlll.configlib.NameFormatters;
import gg.moonrise.engine.config.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigFileTest {

    @TempDir
    private Path tempDir;

    @Test
    void writesSingleScopedConfigFileWithoutRemovedLegacySections() throws Exception {
        Configuration<Config> configuration = loadConfig();

        assertTrue(configuration.get().getChatItemSettings().isEnabled());
        String content = Files.readString(configFile());

        assertTrue(content.contains("general:"));
        assertTrue(content.contains("chat:"));
        assertTrue(content.contains("storage:"));
        assertTrue(content.contains("logging-settings:"));
        assertTrue(content.contains("max-message-length: 1024"));
        assertTrue(content.contains("chat-item-settings:"));
        assertTrue(content.contains("mention-settings:"));
        assertTrue(content.contains("anti-message-signing-settings:"));
        assertTrue(content.contains("rewrite-player-chat: true"));
        assertTrue(content.contains("send-prevents-chat-reports-to-client: true"));
        assertTrue(content.contains("claim-secure-chat-enforced: false"));
        assertTrue(content.contains("database-settings:"));
        assertTrue(content.contains("blocked-item-types:"));
        assertTrue(content.contains("- CAVE_AIR"));
        assertTrue(content.contains("- VOID_AIR"));
        assertTrue(content.contains("username: spiritchat"));
        assertTrue(content.contains("password: change-me"));

        assertFalse(content.contains("player-display-settings"));
        assertFalse(content.contains("check-for-updates"));
        assertFalse(content.contains("messages:"));
    }

    @Test
    void reloadsEditedScopedConfigValues() throws Exception {
        Files.writeString(configFile(), """
                chat:
                  chat-item-settings:
                    enabled: false
                """);

        Configuration<Config> configuration = loadConfig();

        assertFalse(configuration.get().getChatItemSettings().isEnabled());
    }

    private Configuration<Config> loadConfig() {
        return Configuration.config(configFile().toFile(), Config.class, builder -> {
            builder.setNameFormatter(NameFormatters.LOWER_KEBAB_CASE);
            builder.inputNulls(false);
            builder.outputNulls(false);
            return builder;
        });
    }

    private Path configFile() {
        return tempDir.resolve("config.yml");
    }
}
