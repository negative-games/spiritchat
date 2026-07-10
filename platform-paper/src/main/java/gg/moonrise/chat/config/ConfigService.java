package gg.moonrise.chat.config;

import de.exlll.configlib.NameFormatters;
import gg.moonrise.chat.SpiritChatPlugin;
import gg.moonrise.chat.config.serializer.MessageSerializer;
import gg.moonrise.engine.config.Configuration;
import gg.moonrise.engine.message.Message;
import gg.moonrise.engine.state.Reloadable;
import gg.moonrise.moss.spring.SpringComponent;
import jakarta.annotation.PostConstruct;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.Arrays;

@SpringComponent
public class ConfigService implements Reloadable {

    private final File dataFolder;
    private Configuration<Config> configuration;
    private Configuration<Messages> messages;

    @Autowired
    public ConfigService(SpiritChatPlugin plugin) {
        this(plugin.getDataFolder());
    }

    ConfigService(File dataFolder) {
        this.dataFolder = dataFolder;
    }

    @PostConstruct
    public void init() {
        ConfigFiles files = loadFiles();
        publish(files);
    }

    private <T> Configuration<T> load(String fileName, Class<T> type) {
        return Configuration.config(new File(dataFolder, fileName), type, builder -> {
            builder.setNameFormatter(NameFormatters.LOWER_KEBAB_CASE);
            builder.addSerializer(Message.class, new MessageSerializer());

            builder.inputNulls(false);
            builder.outputNulls(false);

            builder.header("""
                    SpiritChat
                    Documentation: https://github.com/moonrise-studios/spiritchat
                    MiniMessage Documentation: https://webui.advntr.dev/
                    """);

            builder.footer("""
                    Author: ericlmao
                    """);

            return builder;
        });
    }

    @Override
    public void reload() {
        ConfigFiles files = loadFiles();
        publish(files);
    }

    /**
     * Get the current configuration.
     * @return The current configuration.
     */
    public Config get() {
        return configuration.get();
    }

    public Messages messages() {
        return messages.get();
    }

    public void send(CommandSender sender, Message message, TagResolver.Single... placeholders) {
        message.send(sender, prefixedPlaceholders(sender, placeholders));
    }

    public Component component(CommandSender sender, Message message, TagResolver.Single... placeholders) {
        return message.asComponent(sender, prefixedPlaceholders(sender, placeholders));
    }

    private TagResolver.Single[] prefixedPlaceholders(CommandSender sender, TagResolver.Single[] placeholders) {
        TagResolver.Single[] resolved = Arrays.copyOf(placeholders, placeholders.length + 1);
        Message prefix = messages().getPrefix();
        Component prefixComponent = prefix == null ? Component.empty() : prefix.asComponent(sender);
        resolved[placeholders.length] = Placeholder.component("prefix", prefixComponent);
        return resolved;
    }

    private ConfigFiles loadFiles() {
        return new ConfigFiles(
                load("config.yml", Config.class),
                load("messages.yml", Messages.class)
        );
    }

    private void publish(ConfigFiles files) {
        this.configuration = files.configuration();
        this.messages = files.messages();
    }

    private record ConfigFiles(Configuration<Config> configuration, Configuration<Messages> messages) {
    }
}
