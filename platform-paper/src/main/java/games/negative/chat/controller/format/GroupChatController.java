package games.negative.chat.controller.format;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import games.negative.alumina.message.Message;
import games.negative.chat.config.section.chat.GroupChatSettings;
import games.negative.chat.controller.ChatController;
import games.negative.chat.service.LuckPermsService;
import io.papermc.paper.chat.ChatRenderer;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.luckperms.api.model.group.Group;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.LinkedList;
import java.util.UUID;

@Slf4j
public record GroupChatController(ChatController controller, GroupChatSettings settings, LuckPermsService luckPermsService) implements ChatRenderer {

    private static LoadingCache<@NotNull UUID, Message> cache;

    public GroupChatController {
         cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(10))
                .build(key -> {
                    LinkedList<Group> groups = luckPermsService.loadGroups(key);

                    for (Group group : groups) {
                        Option<Message> format = settings.format(group.getName());
                        if (format.isEmpty()) continue;

                        return format.get();
                    }
                    return null;
                });
    }

    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
        Message format = cache.get(source.getUniqueId());
        if (format == null) return ChatRenderer.defaultRenderer().render(source, sourceDisplayName, message, viewer);

        return controller.applyFormat(source, format, message);
    }
}
