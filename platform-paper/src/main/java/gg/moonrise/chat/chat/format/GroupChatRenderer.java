package gg.moonrise.chat.chat.format;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import gg.moonrise.chat.chat.integration.LuckPermsService;
import gg.moonrise.chat.config.ConfigService;
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
    private final ConfigService configService;
    private final LoadingCache<@NotNull UUID, List<String>> cache;

    public GroupChatRenderer(ChatFormatService formatter, ConfigService configService, LuckPermsService luckPermsService) {
        this.formatter = formatter;
        this.configService = configService;

        cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(10))
                .build(luckPermsService::loadGroupNames);
    }

    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
        GroupChatSettings settings = configService.get().getGroupChatSettings();
        Optional<String> format = settings.firstFormat(cache.get(source.getUniqueId()));
        if (format.isEmpty()) return ChatRenderer.defaultRenderer().render(source, sourceDisplayName, message, viewer);

        return formatter.applyFormat(source, sourceDisplayName, format.get(), message);
    }
}
