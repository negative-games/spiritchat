package gg.moonrise.chat.storage;

import gg.moonrise.chat.service.SqlStorageService;
import gg.moonrise.moss.spring.SpringComponent;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SpringComponent
@RequiredArgsConstructor
public class PlayerOptionsRepository {

    private final SqlStorageService storage;

    public CompletableFuture<Optional<Boolean>> findMentionPinging(UUID playerId) {
        if (!storage.isAvailable()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        return storage.supplyAsync(() -> {
            try (Connection connection = storage.connection();
                 PreparedStatement statement = connection.prepareStatement("""
                         SELECT mention_pinging
                         FROM spiritchat_player_options
                         WHERE uuid = ?
                         """)) {
                statement.setString(1, playerId.toString());

                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        return Optional.empty();
                    }

                    return Optional.of(result.getBoolean("mention_pinging"));
                }
            }
        });
    }

    public CompletableFuture<Void> saveMentionPinging(UUID playerId, boolean enabled) {
        if (!storage.isAvailable()) {
            return CompletableFuture.failedFuture(new IllegalStateException("SQL storage is not available"));
        }

        return storage.runAsync(() -> {
            try (Connection connection = storage.connection();
                 PreparedStatement statement = connection.prepareStatement(PlayerOptionsSql.upsert(storage.dialect()))) {
                statement.setString(1, playerId.toString());
                statement.setBoolean(2, enabled);
                if (PlayerOptionsSql.upsertParameterCount(storage.dialect()) == 3) {
                    statement.setBoolean(3, enabled);
                }

                statement.executeUpdate();
            }
        });
    }
}
