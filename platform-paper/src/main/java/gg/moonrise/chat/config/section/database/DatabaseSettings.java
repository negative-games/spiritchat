package gg.moonrise.chat.config.section.database;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;

@Getter
@Configuration
public class DatabaseSettings {

    public static final int MIN_POOL_SIZE = 1;
    public static final int MAX_POOL_SIZE = 30;

    @Comment({
            "",
            "Database type for player options and chat logs.",
            "Options: SQLITE, MYSQL, MARIADB, POSTGRESQL",
            " ",
            "Default: SQLITE"
    })
    private DatabaseType type = DatabaseType.SQLITE;

    @Comment({
            "",
            "External database host.",
            "Ignored when type is SQLITE.",
            " ",
            "Default: localhost"
    })
    private String host = "localhost";

    @Comment({
            "",
            "External database port.",
            "Ignored when type is SQLITE.",
            " ",
            "Default: 3306"
    })
    private int port = 3306;

    @Comment({
            "",
            "Database name.",
            "For SQLITE, this is the file name inside the SpiritChat plugin folder.",
            " ",
            "Default: storage.db"
    })
    private String database = "storage.db";

    @Comment({
            "",
            "External database username.",
            "Ignored when type is SQLITE.",
            " ",
            "Default: spiritchat"
    })
    private String username = "spiritchat";

    @Comment({
            "",
            "External database password.",
            "Ignored when type is SQLITE.",
            " ",
            "Default: change-me"
    })
    private String password = "change-me";

    @Comment({
            "",
            "The maximum number of pooled connections.",
            "SQLite always uses a single connection.",
            "External SQL values are clamped between 1 and 30.",
            " ",
            "Default: 10"
    })
    private int poolSize = 10;

    public int boundedPoolSize() {
        return Math.clamp(poolSize, MIN_POOL_SIZE, MAX_POOL_SIZE);
    }
}
