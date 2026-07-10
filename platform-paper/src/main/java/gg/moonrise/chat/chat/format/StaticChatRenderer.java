package gg.moonrise.chat.chat.format;

import gg.moonrise.chat.config.section.chat.StaticChatSettings;
import gg.moonrise.chat.chat.listener.ChatListener;
import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public record StaticChatRenderer(ChatListener listener, StaticChatSettings settings) implements ChatRenderer {

    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
        return listener.formatter().applyFormat(source, sourceDisplayName, settings.effectiveFormat(), message);
    }
}
