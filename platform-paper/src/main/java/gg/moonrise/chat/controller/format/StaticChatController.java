package gg.moonrise.chat.controller.format;

import gg.moonrise.chat.config.section.chat.StaticChatSettings;
import gg.moonrise.chat.controller.ChatController;
import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public record StaticChatController(ChatController controller, StaticChatSettings settings) implements ChatRenderer {

    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
        return controller.applyFormat(source, settings.getFormat(), message);
    }
}
