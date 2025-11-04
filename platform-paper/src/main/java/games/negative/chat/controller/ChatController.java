package games.negative.chat.controller;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ChatController implements Listener {

    public static ChatRenderer GLOBAL_RENDERER;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncChat(AsyncChatEvent event) {
        if (event.isCancelled() || GLOBAL_RENDERER == null) return;

        event.renderer(GLOBAL_RENDERER);
    }

    public static void setGlobalRenderer(ChatRenderer renderer) {
        GLOBAL_RENDERER = renderer;
    }
}
