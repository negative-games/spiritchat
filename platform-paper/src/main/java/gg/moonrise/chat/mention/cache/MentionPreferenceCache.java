package gg.moonrise.chat.mention.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class MentionPreferenceCache {

    private final Cache<UUID, Boolean> preferences = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterAccess(Duration.ofMinutes(30))
            .build();
    private final ConcurrentHashMap<UUID, Long> generations = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public boolean getOrDefault(UUID playerId, boolean defaultValue) {
        Boolean cached = preferences.getIfPresent(playerId);
        return cached == null ? defaultValue : cached;
    }

    public long beginLoad(UUID playerId) {
        return generations.getOrDefault(playerId, 0L);
    }

    public void completeLoad(UUID playerId, long loadGeneration, Optional<Boolean> loaded, boolean online) {
        if (!isCurrent(playerId, loadGeneration)) return;

        if (!online || loaded.isEmpty()) {
            preferences.invalidate(playerId);
            return;
        }

        preferences.put(playerId, loaded.get());
    }

    public long beginSave(UUID playerId, boolean enabled) {
        long generation = sequence.incrementAndGet();
        generations.put(playerId, generation);
        preferences.put(playerId, enabled);
        return generation;
    }

    public void completeSave(UUID playerId, long saveGeneration, boolean online) {
        if (!online && isCurrent(playerId, saveGeneration)) {
            invalidate(playerId);
        }
    }

    public void failSave(UUID playerId, long saveGeneration) {
        if (isCurrent(playerId, saveGeneration)) {
            invalidate(playerId);
        }
    }

    public void invalidate(UUID playerId) {
        preferences.invalidate(playerId);
        generations.remove(playerId);
    }

    public void invalidateAll() {
        preferences.invalidateAll();
        generations.clear();
    }

    private boolean isCurrent(UUID playerId, long generation) {
        return generations.getOrDefault(playerId, 0L) == generation;
    }
}
