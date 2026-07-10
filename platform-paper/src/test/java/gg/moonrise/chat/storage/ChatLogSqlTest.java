package gg.moonrise.chat.storage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatLogSqlTest {

    @Test
    void mysqlDialectsUseCollationFriendlySenderNameLookup() {
        assertUsesPlainSenderNameLookup(SqlDialect.MYSQL);
        assertUsesPlainSenderNameLookup(SqlDialect.MARIADB);
    }

    @Test
    void sqliteAndPostgresUseNormalizedSenderNameLookup() {
        assertUsesNormalizedSenderNameLookup(SqlDialect.SQLITE);
        assertUsesNormalizedSenderNameLookup(SqlDialect.POSTGRESQL);
    }

    private void assertUsesPlainSenderNameLookup(SqlDialect dialect) {
        String sql = ChatLogSql.senderNameLookup(dialect);

        assertTrue(sql.contains("WHERE sender_name = ?"));
        assertFalse(sql.contains("LOWER(sender_name)"));
    }

    private void assertUsesNormalizedSenderNameLookup(SqlDialect dialect) {
        String sql = ChatLogSql.senderNameLookup(dialect);

        assertTrue(sql.contains("WHERE LOWER(sender_name) = LOWER(?)"));
    }
}
