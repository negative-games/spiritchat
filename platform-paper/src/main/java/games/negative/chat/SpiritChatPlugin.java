package games.negative.chat;

import de.exlll.configlib.NameFormatters;
import games.negative.alumina.AluminaPlugin;
import games.negative.alumina.config.Configuration;
import games.negative.chat.command.CommandSpiritChat;
import games.negative.chat.config.Config;
import games.negative.chat.config.section.chat.StaticChatSettings;
import games.negative.chat.controller.ChatController;
import games.negative.chat.controller.format.StaticChatController;
import games.negative.chat.util.LPUtil;
import io.vavr.control.Option;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.luckperms.api.LuckPerms;

import java.io.File;

@Slf4j
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

        reloadConfigs();

        registerListener(new ChatController());

        registerCommand(new CommandSpiritChat());
    }

    @Override
    public void disable() {

    }

    public void reloadConfigs() {
        this.configuration.reload();
        initGlobalChatRenderer();
    }

    private void initGlobalChatRenderer() {
        Config config = config();

        StaticChatSettings staticChatSettings = config.getStaticChatSettings();
        if (staticChatSettings.isEnabled()) {
            ChatController.setGlobalRenderer(new StaticChatController(staticChatSettings));
            log.info("Successfully initialized Static Chat Renderer.");
            return;
        }

        ChatController.setGlobalRenderer(null);
        log.error("Could not initialize a Chat Renderer. Global chat messages will not be formatted.");
    }

    public static Option<LuckPerms> luckperms() {
        return Option.of(luckPerms);
    }

    public static Config config() {
        return getInstance().configuration.get();
    }
}
