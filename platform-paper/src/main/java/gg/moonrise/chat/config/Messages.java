package gg.moonrise.chat.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import gg.moonrise.engine.message.Message;
import lombok.Getter;

@Getter
@Configuration
public class Messages {

    @Comment({
            "Reusable prefix for messages that include the <prefix> placeholder.",
            "Messages only show this prefix when they explicitly contain <prefix>."
    })
    private Message prefix = Message.of("<aqua><bold>SpiritChat</bold></aqua> <dark_gray>»</dark_gray> ");

    @Comment({
            "",
            "General command messages."
    })
    private General general = new General();

    @Comment({
            "",
            "Mention command and ping messages."
    })
    private Mentions mentions = new Mentions();

    @Comment({
            "",
            "Admin chat log lookup messages."
    })
    private Logs logs = new Logs();

    @Getter
    @Configuration
    public static class General {

        @Comment({
                "Message shown when a player or console runs /chat or /spiritchat."
        })
        private Message help = Message.of("""
                <aqua><bold>SpiritChat</bold></aqua>
                <gray>/chat mentions \\<on|off></gray>""".stripTrailing());

        @Comment({
                "",
                "Message shown when an admin runs /chat or /spiritchat."
        })
        private Message adminHelp = Message.of("""
                <aqua><bold>SpiritChat</bold></aqua>
                <gray>/chat mentions \\<on|off></gray>
                <gray>/chat logs recent [page]</gray>
                <gray>/chat logs player \\<name> [page]</gray>
                <gray>/chat logs uuid \\<uuid> [page]</gray>
                <gray>/chat reload</gray>""".stripTrailing());

        @Comment({
                "",
                "Message sent after a successful /chat reload or /spiritchat reload."
        })
        private Message reloadSuccess = Message.of("<prefix><green>Reloaded.");

        @Comment({
                "",
                "Message sent when /chat reload fails."
        })
        private Message reloadFailed = Message.of("<prefix><red>Reload failed. Check the console for details.");

        @Comment({
                "",
                "Message sent when /chat reload is used incorrectly."
        })
        private Message reloadUsage = Message.of("<prefix><red>Usage: /chat reload");

        @Comment({
                "",
                "Message sent when a player-only command is used by console."
        })
        private Message playerOnly = Message.of("<prefix><red>Only players can use this command.");

        @Comment({
                "",
                "Message sent to players when Paper is enforcing secure profiles.",
                "SpiritChat cannot receive unsigned chat in this state. The server owner must update server.properties and restart."
        })
        private Message secureProfileEnforced = Message.of("<prefix><red>Chat may be blocked by server.properties. Ask an administrator to set enforce-secure-profile=false and restart the server.");
    }

    @Getter
    @Configuration
    public static class Mentions {

        @Comment({
                "Message sent when mentions are disabled in config.yml."
        })
        private Message featureDisabled = Message.of("<prefix><red>Mentions are disabled on this server.");

        @Comment({
                "",
                "Message sent when mention player options are disabled in config.yml."
        })
        private Message optionsDisabled = Message.of("<prefix><red>Mention options are disabled on this server.");

        @Comment({
                "",
                "Message sent when a mention option cannot be saved."
        })
        private Message saveFailed = Message.of("<prefix><red>Could not save your mention preference. Try again later.");

        @Comment({
                "",
                "Message sent when /chat mentions is used incorrectly."
        })
        private Message usage = Message.of("<prefix><red>Usage: /chat mentions \\<on|off>");

        @Comment({
                "",
                "Message sent when a player enables mention pings."
        })
        private Message enabled = Message.of("<prefix><green>Mention pings enabled.");

        @Comment({
                "",
                "Message sent when a player disables mention pings."
        })
        private Message disabled = Message.of("<prefix><yellow>Mention pings disabled.");

        @Comment({
                "",
                "Action bar message sent to mentioned players.",
                "Supports <player>."
        })
        private Message actionBar = Message.of("<prefix><yellow><player> mentioned you.");
    }

    @Getter
    @Configuration
    public static class Logs {

        @Comment({
                "Message sent when /chat logs is used without a lookup mode or with missing arguments."
        })
        private Message usage = Message.of("""
                <aqua><bold>SpiritChat Logs</bold></aqua>
                <yellow>/chat logs recent [page]</yellow>
                <yellow>/chat logs player \\<name> [page]</yellow>
                <yellow>/chat logs uuid \\<uuid> [page]</yellow>""".stripTrailing());

        @Comment({
                "",
                "Message sent when chat log lookup commands are used while logging is disabled in config.yml."
        })
        private Message disabled = Message.of("<prefix><red>Chat logs are disabled in the config.");

        @Comment({
                "",
                "Message sent when a log page argument is invalid.",
                "Supports <min> and <max>."
        })
        private Message invalidPage = Message.of("<prefix><red>Log page must be between <min> and <max>.");

        @Comment({
                "",
                "Message sent when storage is unavailable."
        })
        private Message storageUnavailable = Message.of("<prefix><red>Chat log storage is unavailable. Check the console for storage errors.");

        @Comment({
                "",
                "Message sent when a UUID argument is invalid.",
                "Supports <uuid>."
        })
        private Message invalidUuid = Message.of("<prefix><red>Invalid UUID: <white><uuid></white>.");

        @Comment({
                "",
                "Message sent when a log lookup returns no rows."
        })
        private Message noneFound = Message.of("<prefix><yellow>No chat logs found.");

        @Comment({
                "",
                "Header sent before log rows.",
                "Supports <count>, <page>, and <page_size>."
        })
        private Message header = Message.of("<gray>Showing <white><count></white> chat log entries from page <white><page></white>:");

        @Comment({
                "",
                "Format for each chat log row.",
                "Supports <id>, <time>, <player>, <uuid>, <world>, and <message>."
        })
        private Message entry = Message.of("<dark_gray>[<gray><time></gray>]</dark_gray> <white><player></white> <dark_gray>(<world>)</dark_gray>: <gray><message></gray>");

        @Comment({
                "",
                "Footer sent when another page may be available.",
                "Supports <command> and <next_page>."
        })
        private Message nextPage = Message.of("<gray>Next page: <white><command> <next_page></white>");
    }
}
