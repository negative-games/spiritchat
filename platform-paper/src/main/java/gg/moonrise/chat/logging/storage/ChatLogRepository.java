package gg.moonrise.chat.logging.storage;

import gg.moonrise.chat.logging.model.ChatLogEntry;
import gg.moonrise.chat.logging.model.ChatLogPage;
import gg.moonrise.chat.storage.SqlStorageService;
import gg.moonrise.moss.spring.SpringComponent;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SpringComponent
@RequiredArgsConstructor
public class ChatLogRepository {

    private final SqlStorageService storage;

    public CompletableFuture<Void> insert(ChatLogEntry entry) {
        return storage.runAsync(() -> {
            try (Connection connection = storage.connection();
                 PreparedStatement statement = connection.prepareStatement("""
                         INSERT INTO spiritchat_chat_logs (id, sender_uuid, sender_name, message, world, server_time)
                         VALUES (?, ?, ?, ?, ?, ?)
                         """)) {
                statement.setString(1, entry.id().toString());
                statement.setString(2, entry.senderId().toString());
                statement.setString(3, entry.senderName());
                statement.setString(4, entry.message());
                statement.setString(5, entry.world());
                statement.setLong(6, entry.serverTime());
                statement.executeUpdate();
            }
        });
    }

    public CompletableFuture<List<ChatLogEntry>> findRecentPage(int page, int pageSize) {
        ChatLogPage logPage = new ChatLogPage(page, pageSize);
        return storage.supplyAsync(() -> {
            try (Connection connection = storage.connection();
                 PreparedStatement statement = connection.prepareStatement("""
                         SELECT id, sender_uuid, sender_name, message, world, server_time
                         FROM spiritchat_chat_logs
                         ORDER BY server_time DESC, id DESC
                         LIMIT ? OFFSET ?
                         """)) {
                statement.setInt(1, logPage.pageSize());
                statement.setInt(2, logPage.offset());
                return readEntries(statement);
            }
        });
    }

    public CompletableFuture<List<ChatLogEntry>> findBySenderNamePage(String senderName, int page, int pageSize) {
        ChatLogPage logPage = new ChatLogPage(page, pageSize);
        return storage.supplyAsync(() -> {
            try (Connection connection = storage.connection();
                 PreparedStatement statement = connection.prepareStatement(ChatLogSql.senderNameLookup(storage.dialect()))) {
                statement.setString(1, senderName);
                statement.setInt(2, logPage.pageSize());
                statement.setInt(3, logPage.offset());
                return readEntries(statement);
            }
        });
    }

    public CompletableFuture<List<ChatLogEntry>> findBySenderIdPage(UUID senderId, int page, int pageSize) {
        ChatLogPage logPage = new ChatLogPage(page, pageSize);
        return storage.supplyAsync(() -> {
            try (Connection connection = storage.connection();
                 PreparedStatement statement = connection.prepareStatement("""
                         SELECT id, sender_uuid, sender_name, message, world, server_time
                         FROM spiritchat_chat_logs
                         WHERE sender_uuid = ?
                         ORDER BY server_time DESC, id DESC
                         LIMIT ? OFFSET ?
                         """)) {
                statement.setString(1, senderId.toString());
                statement.setInt(2, logPage.pageSize());
                statement.setInt(3, logPage.offset());
                return readEntries(statement);
            }
        });
    }

    private List<ChatLogEntry> readEntries(PreparedStatement statement) throws java.sql.SQLException {
        List<ChatLogEntry> entries = new ArrayList<>();
        try (ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                entries.add(new ChatLogEntry(
                        UUID.fromString(result.getString("id")),
                        UUID.fromString(result.getString("sender_uuid")),
                        result.getString("sender_name"),
                        result.getString("message"),
                        result.getString("world"),
                        result.getLong("server_time")
                ));
            }
        }
        return entries;
    }

}
