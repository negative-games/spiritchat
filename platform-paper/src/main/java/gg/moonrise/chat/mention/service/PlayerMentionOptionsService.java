package gg.moonrise.chat.mention.service;

import gg.moonrise.chat.config.section.chat.MentionSettings;
import gg.moonrise.chat.config.ConfigService;
import gg.moonrise.chat.mention.cache.MentionPreferenceCache;
import gg.moonrise.chat.storage.PlayerOptionsRepository;
import gg.moonrise.engine.state.Reloadable;
import gg.moonrise.moss.spring.Disableable;
import gg.moonrise.moss.spring.SpringComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@SpringComponent
@RequiredArgsConstructor
public class PlayerMentionOptionsService implements Disableable, Reloadable {

    private final ConfigService configService;
    private final PlayerOptionsRepository repository;
    private final MentionPreferenceCache mentionPinging = new MentionPreferenceCache();

    public boolean isPingingEnabled(Player player) {
        MentionSettings settings = configService.get().getMentionSettings();
        if (!settings.isPlayerOptions()) return settings.isDefaultPlayerPinging();

        return mentionPinging.getOrDefault(player.getUniqueId(), settings.isDefaultPlayerPinging());
    }

    public void preload(Player player) {
        UUID playerId = player.getUniqueId();
        long generation = mentionPinging.beginLoad(playerId);
        repository.findMentionPinging(playerId)
                .thenAccept(value -> mentionPinging.completeLoad(playerId, generation, value, player.isOnline()))
                .exceptionally(throwable -> {
                    log.warn("Failed to load mention options for {}.", playerId, throwable);
                    return null;
                });
    }

    public CompletableFuture<Void> setPingingEnabled(Player player, boolean enabled) {
        UUID playerId = player.getUniqueId();
        long generation = mentionPinging.beginSave(playerId, enabled);
        return repository.saveMentionPinging(playerId, enabled)
                .thenRun(() -> mentionPinging.completeSave(playerId, generation, player.isOnline()))
                .exceptionally(throwable -> {
                    mentionPinging.failSave(playerId, generation);
                    log.warn("Failed to save mention options for {}.", playerId, throwable);
                    throw new IllegalStateException("Failed to save mention options", throwable);
                });
    }

    public void invalidate(Player player) {
        mentionPinging.invalidate(player.getUniqueId());
    }

    @Override
    public void reload() {
        // Keep already loaded options across config reloads. Online players are preloaded by MentionService.
    }

    @Override
    public void onDisable() {
        mentionPinging.invalidateAll();
    }
}
