package gg.moonrise.chat.service;

import gg.moonrise.engine.state.Reloadable;
import gg.moonrise.moss.spring.SpringComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SpringComponent
public class OnlinePlayerNameService implements Listener, Reloadable {

    private final Map<UUID, String> names = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        track(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        names.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public void reload() {
        names.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            track(player);
        }
    }

    public Iterable<String> suggestions(String input) {
        String normalized = input == null ? "" : input.toLowerCase(Locale.ROOT);
        return names.values().stream()
                .filter(name -> normalized.isBlank() || name.toLowerCase(Locale.ROOT).startsWith(normalized))
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    private void track(Player player) {
        names.put(player.getUniqueId(), player.getName());
    }
}
