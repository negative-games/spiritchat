package gg.moonrise.chat.storage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerOptionsSqlTest {

    @Test
    void mysqlDialectsUseDuplicateKeyUpsert() {
        assertUsesDuplicateKeyUpsert(SqlDialect.MYSQL);
        assertUsesDuplicateKeyUpsert(SqlDialect.MARIADB);
    }

    @Test
    void sqliteAndPostgresUseConflictUpsert() {
        assertUsesConflictUpsert(SqlDialect.SQLITE);
        assertUsesConflictUpsert(SqlDialect.POSTGRESQL);
    }

    private void assertUsesDuplicateKeyUpsert(SqlDialect dialect) {
        assertTrue(PlayerOptionsSql.upsert(dialect).contains("ON DUPLICATE KEY UPDATE"));
        assertEquals(3, PlayerOptionsSql.upsertParameterCount(dialect));
    }

    private void assertUsesConflictUpsert(SqlDialect dialect) {
        assertTrue(PlayerOptionsSql.upsert(dialect).contains("ON CONFLICT(uuid) DO UPDATE"));
        assertEquals(2, PlayerOptionsSql.upsertParameterCount(dialect));
    }
}
