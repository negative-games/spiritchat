package software.lmao.spiritchat.listener;

import com.google.common.base.Preconditions;
import games.negative.alumina.logger.Logs;
import games.negative.alumina.message.Message;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import software.lmao.spiritchat.SpiritChatPlugin;
import software.lmao.spiritchat.config.SpiritChatConfig;
import software.lmao.spiritchat.permission.Perm;

public class PlayerChatListener implements Listener {

    @EventHandler
    public void onChat(@NotNull AsyncChatEvent event) {
        boolean useStaticFormat = format().useStaticFormat();

        event.renderer(useStaticFormat ? new StaticGlobalChatRenderer() : new GroupGlobalChatRenderer());
    }

    public SpiritChatConfig.Format format() {
        return SpiritChatPlugin.config().format();
    }

    private class StaticGlobalChatRenderer implements ChatRenderer {

        @Override
        public @NotNull Component render(@NotNull Player source, @NotNull Component display, @NotNull Component message, @NotNull Audience viewer) {
            String format = format().globalFormat().orElse(null);
            if (format == null || format.isBlank()) {
                Logs.error("Could not send chat message because 'global-format' is blank or does not exist, yet 'use-static-format' is true!");
                throw new IllegalStateException("Empty global-chat format!");
            }

            Message.Builder builder = new Message(format).create()
                    .replace("%display-name%", display)
                    .replace("%username%", source.getName());

            if (source.hasPermission(Perm.CHAT_COLORS)) {
                builder = builder.replace("%message%", PlainTextComponentSerializer.plainText().serialize(message));
            } else {
                builder = builder.replace("%message%", message);
            }

            return builder.asComponent(source);
        }
    }

    private static class GroupGlobalChatRenderer implements ChatRenderer {

        @Override
        public Component render(@NotNull Player source, @NotNull Component display, @NotNull Component message, @NotNull Audience viewer) {
            return null;
        }
    }
}
