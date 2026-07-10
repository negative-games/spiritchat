package gg.moonrise.chat.storage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlSchemaTest {

    @Test
    void sqliteUsesConnectionPragmasAndIntegerBooleans() {
        assertEquals(3, SqlSchema.connectionStatements(SqlDialect.SQLITE).size());

        String tables = String.join("\n", SqlSchema.tableStatements(SqlDialect.SQLITE));
        assertTrue(tables.contains("mention_pinging INTEGER NOT NULL"));
    }

    @Test
    void mysqlDialectsUseInlineIndexesAndNoSeparateIndexes() {
        assertMySqlSchema(SqlDialect.MYSQL);
        assertMySqlSchema(SqlDialect.MARIADB);
    }

    @Test
    void postgresUsesBooleanTypeAndSeparateLowerNameIndex() {
        String tables = String.join("\n", SqlSchema.tableStatements(SqlDialect.POSTGRESQL));
        String indexes = String.join("\n", SqlSchema.indexStatements(SqlDialect.POSTGRESQL));

        assertTrue(tables.contains("mention_pinging BOOLEAN NOT NULL"));
        assertTrue(indexes.contains("LOWER(sender_name)"));
    }

    private void assertMySqlSchema(SqlDialect dialect) {
        String tables = String.join("\n", SqlSchema.tableStatements(dialect));

        assertTrue(tables.contains("ENGINE=InnoDB"));
        assertTrue(tables.contains("DEFAULT CHARSET=utf8mb4"));
        assertTrue(tables.contains("KEY idx_spiritchat_chat_logs_sender_name_time"));
        assertEquals(0, SqlSchema.indexStatements(dialect).size());
    }
}
