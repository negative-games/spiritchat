package gg.moonrise.chat.storage;

import gg.moonrise.chat.config.section.database.DatabaseSettings;

public final class SqlStorageSizing {

    private SqlStorageSizing() {
    }

    public static int connectionPoolSize(DatabaseSettings settings, SqlDialect dialect) {
        if (dialect == SqlDialect.SQLITE) return 1;

        return settings.boundedPoolSize();
    }

    public static int workerThreads(DatabaseSettings settings, SqlDialect dialect) {
        return connectionPoolSize(settings, dialect);
    }
}
