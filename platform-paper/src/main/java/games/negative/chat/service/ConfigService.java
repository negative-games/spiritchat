package games.negative.chat.service;

import de.exlll.configlib.NameFormatters;
import games.negative.alumina.config.Configuration;
import games.negative.chat.SpiritChatPlugin;
import games.negative.chat.config.Config;
import games.negative.chat.spring.Reloadable;
import games.negative.chat.spring.SpringComponent;

import java.io.File;

@SpringComponent
public class ConfigService implements Reloadable {

    private final SpiritChatPlugin plugin;
    private final Configuration<Config> configuration;

    public ConfigService(SpiritChatPlugin plugin) {
        this.plugin = plugin;

        this.configuration = Configuration.config(new File(plugin.getDataFolder(), "config.yml"), Config.class, builder -> {
            builder.setNameFormatter(NameFormatters.LOWER_KEBAB_CASE);

            builder.inputNulls(true);
            builder.outputNulls(false);

            builder.header("""
           ------------------------------------------------------------------------------------------ \s
                       _____           _          _   _         _____   _               _  \s
                      / ____|         (_)        (_) | |       / ____| | |             | | \s
                     | (___    _ __    _   _ __   _  | |_     | |      | |__     __ _  | |_\s
                      \\___ \\  | '_ \\  | | | '__| | | | __|    | |      | '_ \\   / _` | | __|
                      ____) | | |_) | | | | |    | | | |_     | |____  | | | | | (_| | | |_\s
                     |_____/  | .__/  |_| |_|    |_|  \\__|     \\_____| |_| |_|  \\__,_|  \\__|
                              | |                                                          \s
                              |_|                                                          \s
                             \s
            Documentation: https://docs.negative.games/spiritchat                            \s
            MiniMessage Documentation: https://webui.advntr.dev/           \s
           ------------------------------------------------------------------------------------------""");

            builder.footer("""
                    Author: ericlmao
                    """);

            return builder;
        });
    }

    @Override
    public void onReload() {
        configuration.reload();
    }

    /**
     * Get the current configuration.
     * @return The current configuration.
     */
    public Config get() {
        return configuration.get();
    }
}
