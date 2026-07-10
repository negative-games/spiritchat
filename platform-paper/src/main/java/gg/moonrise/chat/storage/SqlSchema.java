package gg.moonrise.chat.storage;

import java.util.List;

public final class SqlSchema {

    private SqlSchema() {
    }

    public static List<String> connectionStatements(SqlDialect dialect) {
        if (dialect != SqlDialect.SQLITE) return List.of();

        return List.of(
                "PRAGMA busy_timeout = 5000",
                "PRAGMA journal_mode = WAL",
                "PRAGMA synchronous = NORMAL"
        );
    }

    public static List<String> tableStatements(SqlDialect dialect) {
        String booleanType = dialect.usesSqliteBooleans() ? "INTEGER" : "BOOLEAN";
        if (dialect == SqlDialect.MYSQL || dialect == SqlDialect.MARIADB) {
            return List.of(
                    """
                    CREATE TABLE IF NOT EXISTS spiritchat_player_options (
                        uuid CHAR(36) PRIMARY KEY,
                        mention_pinging %s NOT NULL
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                    """.formatted(booleanType),
                    """
                    CREATE TABLE IF NOT EXISTS spiritchat_chat_logs (
                        id CHAR(36) PRIMARY KEY,
                        sender_uuid CHAR(36) NOT NULL,
                        sender_name VARCHAR(16) NOT NULL,
                        message TEXT NOT NULL,
                        world VARCHAR(64),
                        server_time BIGINT NOT NULL,
                        KEY idx_spiritchat_chat_logs_server_time (server_time DESC, id DESC),
                        KEY idx_spiritchat_chat_logs_sender_time (sender_uuid, server_time DESC, id DESC),
                        KEY idx_spiritchat_chat_logs_sender_name_time (sender_name, server_time DESC, id DESC)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                    """
            );
        }

        return List.of(
                """
                CREATE TABLE IF NOT EXISTS spiritchat_player_options (
                    uuid CHAR(36) PRIMARY KEY,
                    mention_pinging %s NOT NULL
                )
                """.formatted(booleanType),
                """
                CREATE TABLE IF NOT EXISTS spiritchat_chat_logs (
                    id CHAR(36) PRIMARY KEY,
                    sender_uuid CHAR(36) NOT NULL,
                    sender_name VARCHAR(16) NOT NULL,
                    message TEXT NOT NULL,
                    world VARCHAR(64),
                    server_time BIGINT NOT NULL
                )
                """
        );
    }

    public static List<String> indexStatements(SqlDialect dialect) {
        return switch (dialect) {
            case MYSQL, MARIADB -> List.of();
            case SQLITE, POSTGRESQL -> List.of(
                    "CREATE INDEX IF NOT EXISTS idx_spiritchat_chat_logs_server_time ON spiritchat_chat_logs(server_time DESC, id DESC)",
                    "CREATE INDEX IF NOT EXISTS idx_spiritchat_chat_logs_sender_time ON spiritchat_chat_logs(sender_uuid, server_time DESC, id DESC)",
                    "CREATE INDEX IF NOT EXISTS idx_spiritchat_chat_logs_sender_name_time ON spiritchat_chat_logs(sender_name, server_time DESC, id DESC)",
                    "CREATE INDEX IF NOT EXISTS idx_spiritchat_chat_logs_sender_name_lower_time ON spiritchat_chat_logs(LOWER(sender_name), server_time DESC, id DESC)"
            );
        };
    }
}
