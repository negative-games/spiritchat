package gg.moonrise.chat.chat.item;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import gg.moonrise.chat.config.ConfigService;
import gg.moonrise.chat.config.section.chat.ChatItemSettings;
import gg.moonrise.engine.paper.scheduler.Scheduler;
import gg.moonrise.engine.state.Reloadable;
import gg.moonrise.moss.spring.Disableable;
import gg.moonrise.moss.spring.SpringComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

@Slf4j
@SpringComponent
@RequiredArgsConstructor
public class ChatItemService implements Disableable, Listener, Reloadable {

    private final ConfigService configService;
    private final AtomicLong snapshotSequence = new AtomicLong();
    private final Cache<UUID, ChatItemSnapshot> snapshots = Caffeine.newBuilder()
            .maximumSize(1_000)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build();
    private final Map<String, Pattern> placeholderPatterns = new ConcurrentHashMap<>();

    private boolean shouldShowcaseItem(String input) {
        ChatItemSettings settings = configService.get().getChatItemSettings();
        return settings.isEnabled() && settings.containsChatItemSyntax(input);
    }

    public void prepareSnapshot(Player player, String input) {
        UUID playerId = player.getUniqueId();
        if (!shouldShowcaseItem(input)) {
            snapshots.invalidate(playerId);
            return;
        }

        ItemStack item = snapshotHeldItem(player);
        if (item == null) {
            snapshots.invalidate(playerId);
            return;
        }

        snapshots.put(playerId, new ChatItemSnapshot(item, snapshotSequence.incrementAndGet()));
    }

    public Component apply(Player player, String input, Component component) {
        ChatItemSettings settings = configService.get().getChatItemSettings();
        if (!settings.isEnabled() || !settings.containsChatItemSyntax(input)) return component;

        ChatItemSnapshot snapshot = snapshots.getIfPresent(player.getUniqueId());
        if (snapshot == null || settings.isItemTypeBlocked(snapshot.item().getType())) return component;

        Component formatted = component;
        Component itemComponent = snapshot.item().effectiveName().hoverEvent(snapshot.item().asHoverEvent());
        for (String placeholder : settings.effectivePlaceholders()) {
            formatted = formatted.replaceText(TextReplacementConfig.builder()
                    .match(placeholderPattern(placeholder))
                    .replacement((match, builder) -> Component.text(match.group(1)).append(itemComponent))
                    .build());
        }
        return formatted;
    }

    public void clearCache() {
        snapshots.invalidateAll();
        placeholderPatterns.clear();
    }

    @Override
    public void reload() {
        clearCache();
    }

    public void clearSnapshot(Player player) {
        snapshots.invalidate(player.getUniqueId());
    }

    public void clearSnapshotAfterRender(Player player) {
        UUID playerId = player.getUniqueId();
        ChatItemSnapshot snapshot = snapshots.getIfPresent(playerId);
        if (snapshot == null) return;

        try {
            Scheduler.entity(player).runDelayed(
                    ignored -> clearSnapshotIfCurrent(playerId, snapshot),
                    () -> clearSnapshotIfCurrent(playerId, snapshot),
                    1L
            );
        } catch (RuntimeException exception) {
            clearSnapshotIfCurrent(playerId, snapshot);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        clearSnapshot(event.getPlayer());
    }

    @Override
    public void onDisable() {
        clearCache();
    }

    private ItemStack snapshotHeldItem(Player player) {
        if (Bukkit.isPrimaryThread()) {
            return snapshotHeldItemSync(player);
        }

        CompletableFuture<ItemStack> future = new CompletableFuture<>();
        boolean scheduled = Scheduler.entity(player).execute(
                () -> future.complete(snapshotHeldItemSync(player)),
                () -> future.complete(null),
                1L
        );
        if (!scheduled) return null;

        try {
            return future.get(250, TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException | TimeoutException exception) {
            log.warn("Failed to snapshot held item for chat item formatting.", exception);
            return null;
        }
    }

    private void clearSnapshotIfCurrent(UUID playerId, ChatItemSnapshot snapshot) {
        snapshots.asMap().remove(playerId, snapshot);
    }

    private ItemStack snapshotHeldItemSync(Player player) {
        ChatItemSettings settings = configService.get().getChatItemSettings();
        if (!player.hasPermission(settings.effectivePermission())) {
            return null;
        }

        return player.getInventory().getItemInMainHand().clone();
    }

    private Pattern placeholderPattern(String placeholder) {
        return placeholderPatterns.computeIfAbsent(
                placeholder,
                value -> Pattern.compile("((?<!\\\\)(?:\\\\\\\\)*)" + Pattern.quote(value))
        );
    }

    private record ChatItemSnapshot(ItemStack item, long sequence) {
    }
}
