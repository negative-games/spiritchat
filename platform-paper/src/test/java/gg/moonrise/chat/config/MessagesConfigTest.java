package gg.moonrise.chat.config;

import de.exlll.configlib.NameFormatters;
import gg.moonrise.chat.config.serializer.MessageSerializer;
import gg.moonrise.engine.config.Configuration;
import gg.moonrise.engine.message.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessagesConfigTest {

    @TempDir
    private Path tempDir;

    @Test
    void writesDefaultMessagesWithMessageSerializer() throws Exception {
        Configuration<Messages> configuration = loadMessages();

        assertEquals("<aqua><bold>SpiritChat</bold></aqua> <dark_gray>»</dark_gray> ", configuration.get().getPrefix().content());
        assertEquals("<prefix><green>Reloaded.", configuration.get().getGeneral().getReloadSuccess().content());
        assertEquals("<prefix><red>Reload failed. Check the console for details.", configuration.get().getGeneral().getReloadFailed().content());
        String content = Files.readString(messagesFile());
        assertTrue(content.contains("prefix:"));
        assertTrue(content.contains("reload-success:"));
        assertTrue(content.contains("reload-failed:"));
        assertTrue(content.contains("<prefix><green>Reloaded."));
    }

    @Test
    void publicHelpDoesNotAdvertiseAdminCommands() {
        Messages messages = new Messages();

        assertTrue(messages.getGeneral().getHelp().content().contains("/chat mentions \\<on|off>"));
        assertFalse(messages.getGeneral().getHelp().content().contains("/chat logs"));
        assertFalse(messages.getGeneral().getHelp().content().contains("/chat reload"));
        assertTrue(messages.getGeneral().getAdminHelp().content().contains("/chat logs player \\<name> [page]"));
        assertTrue(messages.getGeneral().getAdminHelp().content().contains("/chat logs uuid \\<uuid> [page]"));
        assertTrue(messages.getGeneral().getAdminHelp().content().contains("/chat logs"));
        assertTrue(messages.getGeneral().getAdminHelp().content().contains("/chat reload"));
        assertFalse(messages.getGeneral().getHelp().content().endsWith("\n"));
        assertFalse(messages.getGeneral().getAdminHelp().content().endsWith("\n"));
        assertTrue(messages.getLogs().getUsage().content().contains("SpiritChat Logs"));
        assertFalse(messages.getLogs().getUsage().content().endsWith("\n"));
    }

    @Test
    void reloadsEditedMessageContent() throws Exception {
        Files.writeString(messagesFile(), """
                general:
                  reload-success: "<green>Reloaded from disk."
                """);

        Configuration<Messages> configuration = loadMessages();

        assertEquals("<green>Reloaded from disk.", configuration.get().getGeneral().getReloadSuccess().content());
    }

    @Test
    void retainsDefaultsForMissingMessageSections() throws Exception {
        Files.writeString(messagesFile(), """
                general:
                  reload-success: "<green>Reloaded from disk."
                """);

        Configuration<Messages> configuration = loadMessages();

        assertEquals("<prefix><red>Usage: /chat mentions \\<on|off>", configuration.get().getMentions().getUsage().content());
        assertTrue(configuration.get().getLogs().getUsage().content().contains("/chat logs player \\<name> [page]"));
        assertTrue(configuration.get().getLogs().getUsage().content().contains("/chat logs uuid \\<uuid> [page]"));
        assertEquals("<prefix><yellow>No chat logs found.", configuration.get().getLogs().getNoneFound().content());
    }

    @Test
    void nullMessageValuesRetainDefaults() throws Exception {
        Files.writeString(messagesFile(), """
                general:
                  reload-success:
                mentions:
                  action-bar:
                logs:
                  none-found:
                """);

        Configuration<Messages> configuration = loadMessages();

        assertEquals("<prefix><green>Reloaded.", configuration.get().getGeneral().getReloadSuccess().content());
        assertEquals("<prefix><yellow><player> mentioned you.", configuration.get().getMentions().getActionBar().content());
        assertEquals("<prefix><yellow>No chat logs found.", configuration.get().getLogs().getNoneFound().content());
    }

    private Configuration<Messages> loadMessages() {
        return Configuration.config(messagesFile().toFile(), Messages.class, builder -> {
            builder.setNameFormatter(NameFormatters.LOWER_KEBAB_CASE);
            builder.addSerializer(Message.class, new MessageSerializer());
            builder.inputNulls(false);
            builder.outputNulls(false);
            return builder;
        });
    }

    private Path messagesFile() {
        return tempDir.resolve("messages.yml");
    }
}
