package gg.moonrise.chat.config.section.database;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseSettingsTest {

    @Test
    void usesProjectSpecificExternalSqlCredentialDefaults() {
        DatabaseSettings settings = new DatabaseSettings();

        assertEquals("spiritchat", settings.getUsername());
        assertEquals("change-me", settings.getPassword());
    }

    @Test
    void boundsExternalSqlPoolSize() throws Exception {
        DatabaseSettings settings = new DatabaseSettings();

        setPoolSize(settings, -5);
        assertEquals(DatabaseSettings.MIN_POOL_SIZE, settings.boundedPoolSize());

        setPoolSize(settings, 500);
        assertEquals(DatabaseSettings.MAX_POOL_SIZE, settings.boundedPoolSize());

        setPoolSize(settings, 12);
        assertEquals(12, settings.boundedPoolSize());
    }

    private void setPoolSize(DatabaseSettings settings, int poolSize) throws Exception {
        Field field = DatabaseSettings.class.getDeclaredField("poolSize");
        field.setAccessible(true);
        field.setInt(settings, poolSize);
    }
}
