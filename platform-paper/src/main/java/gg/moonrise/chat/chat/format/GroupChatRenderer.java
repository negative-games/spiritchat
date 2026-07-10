package gg.moonrise.chat.chat.format;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import gg.moonrise.chat.chat.integration.LuckPermsService;
import gg.moonrise.chat.config.section.chat.GroupChatSettings;
import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class GroupChatRenderer implements ChatRenderer {

    private final ChatFormatService formatter;
    private final GroupChatSettings settings;
    private final LoadingCache<@NotNull UUID, Optional<String>> cache;

    public GroupChatRenderer(ChatFormatService formatter, GroupChatSettings settings, LuckPermsService luckPermsService) {
        this.formatter = formatter;
        this.settings = settings;

        cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(10))
                .build(key -> {
                    List<String> groups = luckPermsService.loadGroupNames(key);
                    return settings.firstFormat(groups);
                });
    }

    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
        Optional<String> format = cache.get(source.getUniqueId());
        if (format.isEmpty()) return ChatRenderer.defaultRenderer().render(source, sourceDisplayName, message, viewer);

        return formatter.applyFormat(source, sourceDisplayName, format.get(), message);
    }
}
