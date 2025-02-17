package software.lmao.spiritchat;

import com.google.common.base.Preconditions;
import de.exlll.configlib.NameFormatters;
import games.negative.alumina.AluminaPlugin;
import games.negative.alumina.config.Configuration;
import games.negative.alumina.logger.Logs;
import games.negative.alumina.util.Tasks;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bstats.bukkit.Metrics;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import software.lmao.spiritchat.config.SpiritChatConfig;
import software.lmao.spiritchat.listener.PlayerChatListener;
import software.lmao.spiritchat.update.UpdateCheckTask;

import java.io.File;
import java.util.Optional;

public class SpiritChatPlugin extends AluminaPlugin {

    private static SpiritChatPlugin instance;

    private Configuration<SpiritChatConfig> config;

    private LuckPerms luckperms;

    private Metrics metrics;

    @Override
    public void load() {
        instance = this;

        config = Configuration.config(new File(getDataFolder(), "main.yml"), SpiritChatConfig.class, builder -> {
            builder.setNameFormatter(NameFormatters.LOWER_KEBAB_CASE);
            builder.inputNulls(true);
            builder.outputNulls(false);

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
    }

    @Override
    public void disable() {
        if (metrics != null) metrics.shutdown();

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
    public static SpiritChatPlugin instance() {
        Preconditions.checkNotNull(instance, "SpiritChatPlugin has not been initialized yet.");

        return instance;
    }

    @NotNull
    public static SpiritChatConfig config() {
        return instance().configuration().get();
    }

    @CheckReturnValue
    public static Optional<LuckPerms> luckperms() {
        return instance().getLuckPerms();
    }
}
