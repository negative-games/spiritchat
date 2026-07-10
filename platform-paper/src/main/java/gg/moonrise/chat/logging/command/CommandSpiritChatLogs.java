package gg.moonrise.chat.logging.command;

import gg.moonrise.chat.config.ConfigService;
import gg.moonrise.chat.logging.model.ChatLogPage;
import gg.moonrise.chat.logging.model.ChatLogEntry;
import gg.moonrise.chat.logging.storage.ChatLogRepository;
import gg.moonrise.chat.util.PlatformTasks;
import gg.moonrise.engine.paper.command.PaperCommand;
import gg.moonrise.moss.spring.SpringComponent;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SpringComponent
@RequiredArgsConstructor
public class CommandSpiritChatLogs implements PaperCommand {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final ConfigService configService;
    private final ChatLogRepository chatLogRepository;

    @Command("chat|spiritchat logs")
    @Permission("spiritchat.admin")
    public void logs(CommandSourceStack source) {
        if (!loggingEnabled(source.getSender())) return;

        sendUsage(source.getSender());
    }

    @Command("chat|spiritchat logs <extra>")
    @Permission("spiritchat.admin")
    public void unknownLogs(CommandSourceStack source, @Argument("extra") @Greedy String extra) {
        if (!loggingEnabled(source.getSender())) return;

        sendUsage(source.getSender());
    }

    @Command("chat|spiritchat logs recent")
    @Permission("spiritchat.admin")
    public void recent(CommandSourceStack source) {
        recent(source, "1");
    }

    @Command("chat|spiritchat logs recent <page>")
    @Permission("spiritchat.admin")
    public void recent(CommandSourceStack source, @Argument(value = "page", suggestions = "log-pages") String pageInput) {
        if (!loggingEnabled(source.getSender())) return;

        Integer page = page(source.getSender(), pageInput);
        if (page == null) return;

        sendLogs(source.getSender(), page, "/chat logs recent", chatLogRepository.findRecentPage(page, ChatLogPage.LOOKAHEAD_PAGE_SIZE));
    }

    @Command("chat|spiritchat logs recent <page> <extra>")
    @Permission("spiritchat.admin")
    public void recentUsage(CommandSourceStack source, @Argument(value = "page", suggestions = "log-pages") String page, @Argument("extra") @Greedy String extra) {
        if (!loggingEnabled(source.getSender())) return;

        sendUsage(source.getSender());
    }

    @Command("chat|spiritchat logs player")
    @Permission("spiritchat.admin")
    public void playerUsage(CommandSourceStack source) {
        if (!loggingEnabled(source.getSender())) return;

        sendUsage(source.getSender());
    }

    @Command("chat|spiritchat logs player <player>")
    @Permission("spiritchat.admin")
    public void player(CommandSourceStack source, @Argument(value = "player", suggestions = "online-players") String player) {
        player(source, player, "1");
    }

    @Command("chat|spiritchat logs player <player> <page>")
    @Permission("spiritchat.admin")
    public void player(CommandSourceStack source, @Argument(value = "player", suggestions = "online-players") String player, @Argument(value = "page", suggestions = "log-pages") String pageInput) {
        if (!loggingEnabled(source.getSender())) return;

        Integer page = page(source.getSender(), pageInput);
        if (page == null) return;

        sendLogs(source.getSender(), page, "/chat logs player " + player, chatLogRepository.findBySenderNamePage(player, page, ChatLogPage.LOOKAHEAD_PAGE_SIZE));
    }

    @Command("chat|spiritchat logs player <player> <page> <extra>")
    @Permission("spiritchat.admin")
    public void playerUsage(CommandSourceStack source, @Argument(value = "player", suggestions = "online-players") String player, @Argument(value = "page", suggestions = "log-pages") String page, @Argument("extra") @Greedy String extra) {
        if (!loggingEnabled(source.getSender())) return;

        sendUsage(source.getSender());
    }

    @Command("chat|spiritchat logs uuid")
    @Permission("spiritchat.admin")
    public void uuidUsage(CommandSourceStack source) {
        if (!loggingEnabled(source.getSender())) return;

        sendUsage(source.getSender());
    }

    @Command("chat|spiritchat logs uuid <uuid>")
    @Permission("spiritchat.admin")
    public void uuid(CommandSourceStack source, @Argument("uuid") String uuid) {
        uuid(source, uuid, "1");
    }

    @Command("chat|spiritchat logs uuid <uuid> <page>")
    @Permission("spiritchat.admin")
    public void uuid(CommandSourceStack source, @Argument("uuid") String uuid, @Argument(value = "page", suggestions = "log-pages") String pageInput) {
        if (!loggingEnabled(source.getSender())) return;

        Integer page = page(source.getSender(), pageInput);
        if (page == null) return;

        UUID playerId;
        try {
            playerId = UUID.fromString(uuid);
        } catch (IllegalArgumentException exception) {
            sendInvalidUuid(source.getSender(), uuid);
            return;
        }

        sendLogs(source.getSender(), page, "/chat logs uuid " + uuid, chatLogRepository.findBySenderIdPage(playerId, page, ChatLogPage.LOOKAHEAD_PAGE_SIZE));
    }

    @Command("chat|spiritchat logs uuid <uuid> <page> <extra>")
    @Permission("spiritchat.admin")
    public void uuidUsage(CommandSourceStack source, @Argument("uuid") String uuid, @Argument(value = "page", suggestions = "log-pages") String page, @Argument("extra") @Greedy String extra) {
        if (!loggingEnabled(source.getSender())) return;

        sendUsage(source.getSender());
    }

    private boolean loggingEnabled(CommandSender sender) {
        if (configService.get().getLoggingSettings().isEnabled()) {
            return true;
        }

        PlatformTasks.run(sender, () -> configService.send(sender, configService.messages().getLogs().getDisabled()));
        return false;
    }

    private void sendUsage(CommandSender sender) {
        PlatformTasks.run(sender, () -> configService.send(sender, configService.messages().getLogs().getUsage()));
    }

    private void sendInvalidUuid(CommandSender sender, String uuid) {
        PlatformTasks.run(sender, () -> configService.send(sender, configService.messages().getLogs().getInvalidUuid(), Placeholder.unparsed("uuid", uuid)));
    }

    private Integer page(CommandSender sender, String input) {
        int page;
        try {
            page = Integer.parseInt(input);
        } catch (NumberFormatException exception) {
            sendInvalidPage(sender);
            return null;
        }

        if (page >= ChatLogPage.MIN_PAGE && page <= ChatLogPage.MAX_PAGE) return page;

        sendInvalidPage(sender);
        return null;
    }

    private void sendInvalidPage(CommandSender sender) {
        PlatformTasks.run(sender, () -> configService.send(
                sender,
                configService.messages().getLogs().getInvalidPage(),
                Placeholder.unparsed("min", Integer.toString(ChatLogPage.MIN_PAGE)),
                Placeholder.unparsed("max", Integer.toString(ChatLogPage.MAX_PAGE))
        ));
    }

    private void sendLogs(CommandSender sender, int page, String command, CompletableFuture<List<ChatLogEntry>> logs) {
        logs.thenAccept(entries -> PlatformTasks.run(sender, () -> sendEntries(sender, entries, page, command)))
                .exceptionally(throwable -> {
                    PlatformTasks.run(sender, () -> configService.send(sender, configService.messages().getLogs().getStorageUnavailable()));
                    return null;
                });
    }

    private void sendEntries(CommandSender sender, List<ChatLogEntry> entries, int page, String command) {
        if (entries.isEmpty()) {
            configService.send(sender, configService.messages().getLogs().getNoneFound());
            return;
        }

        boolean hasNextPage = entries.size() > ChatLogPage.DEFAULT_PAGE_SIZE;
        List<ChatLogEntry> pageEntries = hasNextPage ? entries.subList(0, ChatLogPage.DEFAULT_PAGE_SIZE) : entries;

        configService.send(
                sender,
                configService.messages().getLogs().getHeader(),
                Placeholder.unparsed("count", Integer.toString(pageEntries.size())),
                Placeholder.unparsed("page", Integer.toString(page)),
                Placeholder.unparsed("page_size", Integer.toString(ChatLogPage.DEFAULT_PAGE_SIZE))
        );
        for (ChatLogEntry entry : pageEntries) {
            configService.send(
                    sender,
                    configService.messages().getLogs().getEntry(),
                    Placeholder.unparsed("id", entry.id().toString()),
                    Placeholder.unparsed("time", TIME_FORMAT.format(Instant.ofEpochMilli(entry.serverTime()))),
                    Placeholder.unparsed("player", entry.senderName()),
                    Placeholder.unparsed("uuid", entry.senderId().toString()),
                    Placeholder.unparsed("world", entry.world() == null ? "" : entry.world()),
                    Placeholder.unparsed("message", entry.message())
            );
        }
        if (hasNextPage) {
            configService.send(
                    sender,
                    configService.messages().getLogs().getNextPage(),
                    Placeholder.unparsed("command", command),
                    Placeholder.unparsed("next_page", Integer.toString(page + 1))
            );
        }
    }

    @Suggestions("online-players")
    public Iterable<String> onlinePlayerSuggestions(CommandContext<CommandSourceStack> context, String input) {
        String normalized = input == null ? "" : input.toLowerCase(Locale.ROOT);
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> normalized.isBlank() || name.toLowerCase(Locale.ROOT).startsWith(normalized))
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    @Suggestions("log-pages")
    public Iterable<String> pageSuggestions(CommandContext<CommandSourceStack> context, String input) {
        return List.of("1", "2", "3", "4", "5");
    }
}
