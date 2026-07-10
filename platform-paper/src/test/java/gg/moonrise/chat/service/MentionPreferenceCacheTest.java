package gg.moonrise.chat.service;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MentionPreferenceCacheTest {

    @Test
    void staleLoadCannotOverwriteNewerSave() {
        MentionPreferenceCache cache = new MentionPreferenceCache();
        UUID playerId = UUID.randomUUID();

        long loadGeneration = cache.beginLoad(playerId);
        cache.beginSave(playerId, true);
        cache.completeLoad(playerId, loadGeneration, Optional.of(false), true);

        assertTrue(cache.getOrDefault(playerId, false));
    }

    @Test
    void failedCurrentSaveRollsBackCachedPreference() {
        MentionPreferenceCache cache = new MentionPreferenceCache();
        UUID playerId = UUID.randomUUID();

        long saveGeneration = cache.beginSave(playerId, true);
        cache.failSave(playerId, saveGeneration);

        assertFalse(cache.getOrDefault(playerId, false));
    }

    @Test
    void staleSaveFailureCannotRollbackNewerSave() {
        MentionPreferenceCache cache = new MentionPreferenceCache();
        UUID playerId = UUID.randomUUID();

        long firstSave = cache.beginSave(playerId, false);
        cache.beginSave(playerId, true);
        cache.failSave(playerId, firstSave);

        assertTrue(cache.getOrDefault(playerId, false));
    }

    @Test
    void offlineLoadClearsCurrentCachedPreference() {
        MentionPreferenceCache cache = new MentionPreferenceCache();
        UUID playerId = UUID.randomUUID();

        long loadGeneration = cache.beginLoad(playerId);
        cache.completeLoad(playerId, loadGeneration, Optional.of(true), false);

        assertFalse(cache.getOrDefault(playerId, false));
    }
}
