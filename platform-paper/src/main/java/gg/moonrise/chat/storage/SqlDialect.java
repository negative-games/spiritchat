package gg.moonrise.chat.storage;

import gg.moonrise.chat.config.section.database.DatabaseType;

public enum SqlDialect {

    SQLITE,
    MYSQL,
    MARIADB,
    POSTGRESQL;

    public static SqlDialect from(DatabaseType type) {
        return switch (type == null ? DatabaseType.SQLITE : type) {
            case SQLITE -> SQLITE;
            case MYSQL -> MYSQL;
            case MARIADB, MARIA -> MARIADB;
            case POSTGRESQL -> POSTGRESQL;
        };
    }

    public boolean usesSqliteBooleans() {
        return this == SQLITE;
    }
}
