package gg.moonrise.chat.storage;

import gg.moonrise.chat.config.section.database.DatabaseType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqlDialectTest {

    @Test
    void acceptsMariaDbAndLegacyMariaConfigValues() {
        assertEquals(SqlDialect.MARIADB, SqlDialect.from(DatabaseType.MARIADB));
        assertEquals(SqlDialect.MARIADB, SqlDialect.from(DatabaseType.MARIA));
    }

    @Test
    void defaultsNullDatabaseTypeToSqlite() {
        assertEquals(SqlDialect.SQLITE, SqlDialect.from(null));
    }
}
