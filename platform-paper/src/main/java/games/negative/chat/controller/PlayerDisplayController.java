package games.negative.chat.controller;

import games.negative.chat.SpiritChatPlugin;
import games.negative.chat.config.section.display.PlayerDisplaySettings;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerDisplayController implements Listener {


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!settings().isOverridePlayerDisplayName()) return;

        Player player = event.getPlayer();

    }

    private PlayerDisplaySettings settings() {
        return SpiritChatPlugin.config().getPlayerDisplaySettings();
    }
}
