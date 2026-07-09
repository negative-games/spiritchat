package gg.moonrise.chat.service;

import de.exlll.configlib.NameFormatters;
import gg.moonrise.chat.SpiritChatPlugin;
import gg.moonrise.chat.config.Config;
import gg.moonrise.engine.config.Configuration;
import gg.moonrise.engine.state.Reloadable;
import gg.moonrise.moss.spring.SpringComponent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.List;

@SpringComponent
@RequiredArgsConstructor
public class ConfigService implements Reloadable {

    private final SpiritChatPlugin plugin;
    private Configuration<Config.General> general;
    private Configuration<Config.Chat> chat;
    private Configuration<Config.Database> database;
    private List<Configuration<?>> configurations;
    private volatile Config config;

    @PostConstruct
    public void init() {
        this.general = load("general.yml", Config.General.class);
        this.chat = load("chat.yml", Config.Chat.class);
        this.database = load("database.yml", Config.Database.class);
        this.configurations = List.of(
                general,
                chat,
                database
        );
        compose();
    }

    private <T> Configuration<T> load(String fileName, Class<T> type) {
        return Configuration.config(new File(plugin.getDataFolder(), fileName), type, builder -> {
            builder.setNameFormatter(NameFormatters.LOWER_KEBAB_CASE);

            builder.inputNulls(false);
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
            Documentation: https://github.com/moonrise-studios/spiritchat                  \s
            MiniMessage Documentation: https://webui.advntr.dev/           \s
           ------------------------------------------------------------------------------------------""");

            builder.footer("""
                    Author: ericlmao
                    """);

            return builder;
        });
    }

    @Override
    public void reload() {
        configurations.forEach(Configuration::reload);
        compose();
    }

    /**
     * Get the current configuration.
     * @return The current configuration.
     */
    public Config get() {
        return config;
    }

    private void compose() {
        this.config = Config.compose(general.get(), chat.get(), database.get());
    }
}
