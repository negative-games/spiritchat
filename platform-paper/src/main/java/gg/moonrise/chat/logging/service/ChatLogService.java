package gg.moonrise.chat.logging.service;

import gg.moonrise.chat.config.ConfigService;
import gg.moonrise.chat.logging.model.ChatLogEntry;
import gg.moonrise.chat.logging.storage.ChatLogRepository;
import gg.moonrise.chat.logging.util.UuidV7;
import gg.moonrise.chat.util.PlatformTasks;
import gg.moonrise.engine.message.util.MiniMessageUtil;
import gg.moonrise.moss.spring.SpringComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@Slf4j
@SpringComponent
@RequiredArgsConstructor
public class ChatLogService {

    private final ConfigService configService;
    private final ChatLogRepository repository;

    public void log(Player sender, Component message) {
        if (!configService.get().getLoggingSettings().isEnabled()) return;

        if (Bukkit.isPrimaryThread()) {
            store(snapshot(sender, message));
            return;
        }

        PlatformTasks.run(sender, () -> {
            if (!sender.isOnline()) return;

            store(snapshot(sender, message));
        });
    }

    private ChatLogEntry snapshot(Player sender, Component message) {
        UUID senderId = sender.getUniqueId();
        return new ChatLogEntry(
                UuidV7.generate(),
                senderId,
                sender.getName(),
                truncate(
                        MiniMessageUtil.componentToPlainText(message),
                        configService.get().getLoggingSettings().effectiveMaxMessageLength()
                ),
                sender.getWorld().getName(),
                System.currentTimeMillis()
        );
    }

    private void store(ChatLogEntry entry) {
        repository.insert(entry)
                .exceptionally(throwable -> {
                    log.warn("Failed to store chat log entry for {}.", entry.senderId(), throwable);
                    return null;
                });
    }

    private String truncate(String input, int maxLength) {
        String text = input == null ? "" : input;
        if (text.length() <= maxLength) return text;

        return text.substring(0, maxLength);
    }
}
