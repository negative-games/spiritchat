package gg.moonrise.chat.chat.format;

import gg.moonrise.chat.config.section.chat.StaticChatSettings;
import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public record StaticChatRenderer(ChatFormatService formatter, StaticChatSettings settings) implements ChatRenderer {

    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
        return formatter.applyFormat(source, sourceDisplayName, settings.effectiveFormat(), message);
    }
}
