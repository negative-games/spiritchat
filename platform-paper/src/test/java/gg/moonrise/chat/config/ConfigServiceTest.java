package gg.moonrise.chat.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigServiceTest {

    @TempDir
    private Path tempDir;

    @Test
    void failedReloadKeepsLastPublishedConfigAndMessages() throws Exception {
        ConfigService service = new ConfigService(tempDir.toFile());
        service.init();

        assertTrue(service.get().getStaticChatSettings().isEnabled());
        assertEquals("<prefix><green>Reloaded.", service.messages().getGeneral().getReloadSuccess().content());

        Files.writeString(tempDir.resolve("config.yml"), """
                chat:
                  static-chat-settings:
                    enabled: false
                """);
        Files.writeString(tempDir.resolve("messages.yml"), """
                general:
                  reload-success: [
                """);

        assertThrows(RuntimeException.class, service::reload);
        assertTrue(service.get().getStaticChatSettings().isEnabled());
        assertEquals("<prefix><green>Reloaded.", service.messages().getGeneral().getReloadSuccess().content());
    }
}
