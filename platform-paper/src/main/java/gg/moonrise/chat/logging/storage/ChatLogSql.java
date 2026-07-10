package gg.moonrise.chat.logging.storage;

import gg.moonrise.chat.storage.SqlDialect;

final class ChatLogSql {

    private ChatLogSql() {
    }

    static String senderNameLookup(SqlDialect dialect) {
        if (dialect == SqlDialect.MYSQL || dialect == SqlDialect.MARIADB) {
            return """
                    SELECT id, sender_uuid, sender_name, message, world, server_time
                    FROM spiritchat_chat_logs
                    WHERE sender_name = ?
                    ORDER BY server_time DESC, id DESC
                    LIMIT ? OFFSET ?
                    """;
        }

        return """
                SELECT id, sender_uuid, sender_name, message, world, server_time
                FROM spiritchat_chat_logs
                WHERE LOWER(sender_name) = LOWER(?)
                ORDER BY server_time DESC, id DESC
                LIMIT ? OFFSET ?
                """;
    }
}
