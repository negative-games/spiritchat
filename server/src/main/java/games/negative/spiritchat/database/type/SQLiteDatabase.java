package games.negative.spiritchat.database.type;

import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import games.negative.alumina.logger.Logs;
import games.negative.spiritchat.SpiritChatPlugin;
import games.negative.spiritchat.config.SpiritChatConfig;
import games.negative.spiritchat.database.DatabaseManager;
import games.negative.spiritchat.database.DatabaseType;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLiteDatabase implements DatabaseManager {

    private final HikariDataSource source;

    @SneakyThrows
    public SQLiteDatabase(SpiritChatConfig.Database creds) {
        DatabaseType type = creds.getType();
        Preconditions.checkState(type.equals(DatabaseType.SQLITE), "Could not initialize SQLite database, type is not SQLite");

        File dir = SpiritChatPlugin.instance().getDataFolder();
        File file = new File(dir, "database.db");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (!file.exists()) {
            file.createNewFile();
        }

        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath());
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("allowMultiQueries", "true");
        config.setMaximumPoolSize(10);

        this.source = new HikariDataSource(config);

        createTables();
    }

    @Override
    public void createTables() {
        try (Connection connection = connection()) {
            // ChatColors | <id>, <code/color>
            PreparedStatement chatColors = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `sc_chatcolors` (`id` TEXT NOT NULL PRIMARY KEY, `code` TEXT NOT NULL)");
            chatColors.execute();

            // ChatColor UserData | <uuid>, <chatcolor id/code(if custom)>
            PreparedStatement chatColorUserData = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `sc_chatcolor_users` (`uuid` TEXT NOT NULL PRIMARY KEY, `chatcolor` TEXT NOT NULL)");
            chatColorUserData.execute();
        } catch (SQLException e) {
            Logs.severe("Failed to create SQLite tables: " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(SpiritChatPlugin.instance());
        }
    }

    @Override
    public void disconnect() {
        source.close();
    }

    private Connection connection() throws SQLException {
        return source.getConnection();
    }
}
