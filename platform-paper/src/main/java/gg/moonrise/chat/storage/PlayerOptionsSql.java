package gg.moonrise.chat.storage;

final class PlayerOptionsSql {

    private PlayerOptionsSql() {
    }

    static String upsert(SqlDialect dialect) {
        return switch (dialect) {
            case SQLITE, POSTGRESQL -> """
                    INSERT INTO spiritchat_player_options (uuid, mention_pinging)
                    VALUES (?, ?)
                    ON CONFLICT(uuid) DO UPDATE SET mention_pinging = excluded.mention_pinging
                    """;
            case MYSQL, MARIADB -> """
                    INSERT INTO spiritchat_player_options (uuid, mention_pinging)
                    VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE mention_pinging = ?
                    """;
        };
    }

    static int upsertParameterCount(SqlDialect dialect) {
        return switch (dialect) {
            case SQLITE, POSTGRESQL -> 2;
            case MYSQL, MARIADB -> 3;
        };
    }
}
