package gg.moonrise.chat.storage;

import gg.moonrise.chat.config.section.database.DatabaseSettings;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqlStorageSizingTest {

    @Test
    void sqliteAlwaysUsesSingleConnectionAndWorker() throws Exception {
        DatabaseSettings settings = settingsWithPoolSize(20);

        assertEquals(1, SqlStorageSizing.connectionPoolSize(settings, SqlDialect.SQLITE));
        assertEquals(1, SqlStorageSizing.workerThreads(settings, SqlDialect.SQLITE));
    }

    @Test
    void externalDatabasesUseBoundedPoolSizeForConnectionsAndWorkers() throws Exception {
        DatabaseSettings settings = settingsWithPoolSize(8);

        assertEquals(8, SqlStorageSizing.connectionPoolSize(settings, SqlDialect.MYSQL));
        assertEquals(8, SqlStorageSizing.workerThreads(settings, SqlDialect.MYSQL));
        assertEquals(8, SqlStorageSizing.connectionPoolSize(settings, SqlDialect.MARIADB));
        assertEquals(8, SqlStorageSizing.workerThreads(settings, SqlDialect.MARIADB));
        assertEquals(8, SqlStorageSizing.connectionPoolSize(settings, SqlDialect.POSTGRESQL));
        assertEquals(8, SqlStorageSizing.workerThreads(settings, SqlDialect.POSTGRESQL));
    }

    private DatabaseSettings settingsWithPoolSize(int poolSize) throws Exception {
        DatabaseSettings settings = new DatabaseSettings();
        Field field = DatabaseSettings.class.getDeclaredField("poolSize");
        field.setAccessible(true);
        field.setInt(settings, poolSize);
        return settings;
    }
}
