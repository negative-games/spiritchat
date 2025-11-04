package games.negative.chat;

import de.exlll.configlib.NameFormatters;
import games.negative.alumina.AluminaPlugin;
import games.negative.alumina.config.Configuration;
import games.negative.chat.config.Config;
import games.negative.chat.util.LPUtil;
import io.vavr.control.Option;
import lombok.Getter;
import net.luckperms.api.LuckPerms;

import java.io.File;

public class SpiritChatPlugin extends AluminaPlugin {

    @Getter
    private static SpiritChatPlugin instance;

    @Getter
    private static LuckPerms luckPerms;

    private Configuration<Config> configuration;

    @Override
    public void load() {
        instance = this;

        this.configuration = Configuration.config(new File(getDataFolder(), "config.yml"), Config.class, builder -> {
            builder.setNameFormatter(NameFormatters.LOWER_KEBAB_CASE);

            builder.inputNulls(true);
            builder.outputNulls(false);

            builder.header("""
           ------------------------------------------------------------------------------- \s
                      _____       _      _ _    _____ _           _  \s
                     / ____|     (_)    (_) |  / ____| |         | | \s
                    | (___  _ __  _ _ __ _| |_| |    | |__   __ _| |_\s
                     \\___ \\| '_ \\| | '__| | __| |    | '_ \\ / _` | __|
                     ____) | |_) | | |  | | |_| |____| | | | (_| | |_\s
                    |_____/| .__/|_|_|  |_|\\__|\\_____|_| |_|\\__,_|\\__|
                           | |                                       \s
                           |_|                                       \s
           -------------------------------------------------------------------------------""");

            builder.footer("""
                    Author: ericlmao
                    """);

            return builder;
        });
    }

    @Override
    public void enable() {
        luckPerms = LPUtil.loadLuckPerms();
    }

    @Override
    public void disable() {

    }

    public static Option<LuckPerms> luckperms() {
        return Option.of(luckPerms);
    }

    public static Config config() {
        return getInstance().configuration.get();
    }
}
