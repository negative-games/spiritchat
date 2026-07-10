package gg.moonrise.chat.chat.format;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import gg.moonrise.chat.config.section.chat.GroupChatSettings;
import gg.moonrise.chat.chat.listener.ChatListener;
import gg.moonrise.chat.chat.integration.LuckPermsService;
import io.papermc.paper.chat.ChatRenderer;
import io.vavr.control.Option;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class GroupChatRenderer implements ChatRenderer {

    private final ChatListener listener;
    private final GroupChatSettings settings;
    private final LoadingCache<@NotNull UUID, Optional<String>> cache;

    public GroupChatRenderer(ChatListener listener, GroupChatSettings settings, LuckPermsService luckPermsService) {
        this.listener = listener;
        this.settings = settings;

        cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(10))
                .build(key -> {
                    List<String> groups = luckPermsService.loadGroupNames(key);

                    for (String group : groups) {
                        Option<String> format = settings.format(group);
                        if (format.isEmpty()) continue;

                        return Optional.of(format.get());
                    }
                    return Optional.empty();
                });
    }

    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
        Optional<String> format = cache.get(source.getUniqueId());
        if (format.isEmpty()) return ChatRenderer.defaultRenderer().render(source, sourceDisplayName, message, viewer);

        return listener.formatter().applyFormat(source, sourceDisplayName, format.get(), message);
    }
}
