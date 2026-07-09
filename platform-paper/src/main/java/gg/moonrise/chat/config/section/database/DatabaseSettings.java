package gg.moonrise.chat.config.section.database;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;

@Getter
@Configuration
public class DatabaseSettings {

    @Comment({
            "",
            "The type of database to use for storage purposes.",
            "Options: SQLITE, MYSQL, MARIA, POSTGRESQL",
            " " ,
            "Default: SQLITE"
    })
    private DatabaseType type = DatabaseType.SQLITE;

    @Comment({
            "",
            "The host of the SQL database.",
            " ",
            "Default: localhost"
    })
    private String host = "localhost";

    @Comment({
            "",
            "The port of the SQL database.",
            " ",
            "Default: 3306"
    })
    private int port = 3306;

    @Comment({
            "",
            "The name of the SQL database.",
            " ",
            "Default: spiritchat_db"
    })
    private String database = "spiritchat_db";

    @Comment({
            "",
            "The username for the SQL database.",
            " ",
            "Default: root"
    })
    private String username = "root";

    @Comment({
            "",
            "The password for the SQL database.",
            " ",
            "Default: password"
    })
    private String password = "password";
}
