package games.negative.chat.controller.format;

import games.negative.chat.config.section.chat.StaticChatSettings;
import games.negative.chat.util.ChatUtil;
import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public record StaticChatController(StaticChatSettings settings) implements ChatRenderer {

    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
        String displayName = ChatUtil.MINIMESSAGE.serialize(sourceDisplayName);
        String input = ChatUtil.formatMessage(source, message);

        final String format = settings.getFormat().replace("%player%", displayName)
                .replace("%message%", input);

        return ChatUtil.MINIMESSAGE.deserialize(format);
    }
}
