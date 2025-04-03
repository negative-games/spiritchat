package games.negative.spiritchat;

import com.google.common.base.Preconditions;
import de.exlll.configlib.NameFormatters;
import games.negative.alumina.AluminaPlugin;
import games.negative.alumina.config.Configuration;
import games.negative.alumina.logger.Logs;
import games.negative.alumina.message.Message;
import games.negative.alumina.util.Tasks;
import games.negative.spiritchat.database.DatabaseManager;
import games.negative.spiritchat.database.DatabaseType;
import games.negative.spiritchat.database.type.SQLiteDatabase;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bstats.bukkit.Metrics;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import games.negative.spiritchat.command.CommandSpiritChat;
import games.negative.spiritchat.config.SpiritChatConfig;
import games.negative.spiritchat.config.serializer.MessageSerializer;
import games.negative.spiritchat.listener.PlayerChatListener;
import games.negative.spiritchat.update.UpdateCheckTask;

import java.io.File;
import java.util.Optional;

public class SpiritChatPlugin extends AluminaPlugin {

    private static SpiritChatPlugin instance;

    private Configuration<SpiritChatConfig> config;

    private LuckPerms luckperms;

    private Metrics metrics;

    private DatabaseManager dataManager;

    @Override
    public void load() {
        instance = this;

        config = Configuration.config(new File(getDataFolder(), "main.yml"), SpiritChatConfig.class, builder -> {
            builder.setNameFormatter(NameFormatters.LOWER_KEBAB_CASE);
            builder.inputNulls(true);
            builder.outputNulls(false);

            builder.addSerializer(Message.class, new MessageSerializer());

            builder.header("""
                    |---------------------------------------------|
                    |                SpiritChat                   |
                    |              Version: %s                 |
                    |---------------------------------------------|
                    """.formatted(getPluginMeta().getVersion()));

            builder.footer("""
                    Author: ericlmao
                    """);

            return builder;
        });

        initializeDatabase();
    }

    @Override
    public void enable() {
        // Check for updates every 10 minutes
        Tasks.async(new UpdateCheckTask(getPluginMeta().getVersion()), 20 * 15, 20 * 60 * 10);

        if (config().bStats()) {
            Logs.info("Enabling bStats. Thank you for helping us improve the plugin! You can disable this at any time in the config.");
            this.metrics = new Metrics(this, 24829);
        } else {
            Logs.warning("bStats is disabled. We ask that you have this enabled to help us improve the plugin. This can be changed in the config!");
        }

        loadLuckPerms();

        registerListener(new PlayerChatListener());
        registerCommand(new CommandSpiritChat());
    }

    @Override
    public void disable() {
        if (metrics != null) metrics.shutdown();

    }

    private void initializeDatabase() {
        Logs.info("Initializing database...");
        DatabaseType type = config().database().getType();
        switch (type) {
            case MARIA -> {
                // not implemented!
            }
            default -> {
                this.dataManager = new SQLiteDatabase(config().database());
            }
        }
    }

    public void reload() {
        config.reload();
    }

    private void loadLuckPerms() {
        long start = System.currentTimeMillis();

        try {
            Class.forName("net.luckperms.api.LuckPermsProvider");

            luckperms = LuckPermsProvider.get();

            Logs.info("Successfully loaded LuckPerms support in %sms.".formatted(System.currentTimeMillis() - start));
        } catch (Exception e) {
            Logs.error("Failed to load LuckPerms support! Some features may not work. Install LuckPerms to gain full functionality!");
        }
    }

    @NotNull
    public Configuration<SpiritChatConfig> configuration() {
        return config;
    }

    @CheckReturnValue
    public Optional<LuckPerms> getLuckPerms() {
        return Optional.ofNullable(luckperms);
    }

    @NotNull
    public DatabaseManager getDataManager() {
        return dataManager;
    }

    @NotNull
    public static SpiritChatPlugin instance() {
        Preconditions.checkNotNull(instance, "SpiritChatPlugin has not been initialized yet.");

        return instance;
    }

    @NotNull
    public static SpiritChatConfig config() {
        return instance().configuration().get();
    }

    @NotNull
    public static SpiritChatConfig.Messages messages() {
        return config().messages();
    }

    @CheckReturnValue
    public static Optional<LuckPerms> luckperms() {
        return instance().getLuckPerms();
    }

    public static DatabaseManager database() {
        return instance().getDataManager();
    }
}
