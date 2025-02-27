package games.negative.spiritchat.listener;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import games.negative.alumina.logger.Logs;
import games.negative.alumina.message.Message;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import games.negative.spiritchat.SpiritChatPlugin;
import games.negative.spiritchat.config.SpiritChatConfig;
import games.negative.spiritchat.permission.Perm;

import java.time.Duration;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PlayerChatListener implements Listener {

    @EventHandler
    public void onChat(@NotNull AsyncChatEvent event) {
        boolean useStaticFormat = format().useStaticFormat();

        event.renderer(useStaticFormat ? new StaticGlobalChatRenderer() : new GroupGlobalChatRenderer());
    }

    public static SpiritChatConfig.Format format() {
        return SpiritChatPlugin.config().format();
    }

    private class StaticGlobalChatRenderer implements ChatRenderer {

        @Override
        public @NotNull Component render(@NotNull Player source, @NotNull Component display, @NotNull Component message, @NotNull Audience viewer) {
            String format = format().globalFormat().orElse(null);
            if (format == null || format.isBlank()) {
                Logs.error("Could not send chat message because 'global-format' is blank or does not exist");
                throw new IllegalStateException("Empty global-chat format!");
            }

            Message.Builder builder = new Message(format).create()
                    .replace("%display-name%", display)
                    .replace("%username%", source.getName());

            if (source.hasPermission(Perm.CHAT_COLORS)) {
                TextComponent component = LegacyComponentSerializer.legacyAmpersand().deserialize(PlainTextComponentSerializer.plainText().serialize(message));
                builder = builder.replace("%message%", MiniMessage.miniMessage().serialize(component));
            } else {
                builder = builder.replace("%message%", PlainTextComponentSerializer.plainText().serialize(message));
            }

            ItemStack item = source.getInventory().getItemInMainHand();
            if (format().useItemDisplay() && source.hasPermission(Perm.CHAT_ITEM) && isChatItemSyntax(message) && !item.getType().isAir()) {
                String itemMiniMessage = createItemName(item.displayName());

                builder = builder.replace(Pattern.quote("{i}"), itemMiniMessage);
                builder = builder.replace("\\{item\\}", itemMiniMessage);
            }

            return builder.asComponent(source);
        }
    }

    private class GroupGlobalChatRenderer implements ChatRenderer {

        // Cache results of the highest group (that has a valid format) for 10 seconds
        private static final LoadingCache<UUID, String> CACHE = CacheBuilder.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(10))
                .build(new CacheLoader<>() {
                    @Override
                    public @NotNull String load(@NotNull UUID key) throws Exception {
                        LuckPerms api = SpiritChatPlugin.luckperms().orElse(null);
                        if (api == null) throw new Exception("Could not find LuckPerms dependency on the server!");

                        User user = api.getUserManager().getUser(key);
                        if (user == null) throw new Exception("Could not find user with UUID %s".formatted(key));

                        LinkedList<Group> groups = user.getInheritedGroups(user.getQueryOptions()).stream()
                                .sorted(Comparator.comparingInt(value -> ((Group) value).getWeight().orElse(0)).reversed())
                                .collect(Collectors.toCollection(Lists::newLinkedList));

                        for (Group group : groups) {
                            String format = format().groupFormat(group.getName()).orElse(null);
                            if (format == null || format.isBlank()) continue;

                            return format;
                        }

                        throw new Exception("Could not find a valid group format for user with UUID %s".formatted(key));
                    }
                });

        @Override
        public Component render(@NotNull Player source, @NotNull Component display, @NotNull Component message, @NotNull Audience viewer) {
            LuckPerms api = SpiritChatPlugin.luckperms().orElse(null);
            if (api == null) {
                Logs.error("Could not find LuckPerms dependency on the server, yet 'use-static-format' is false!");
                Logs.info("Defaulting to static format for chat messages.");

                return new StaticGlobalChatRenderer().render(source, display, message, viewer);
            }

            try {
                String format = CACHE.get(source.getUniqueId());

                Message.Builder builder = new Message(format).create()
                        .replace("%display-name%", display)
                        .replace("%username%", source.getName());

                if (source.hasPermission(Perm.CHAT_COLORS)) {
                    TextComponent component = LegacyComponentSerializer.legacyAmpersand().deserialize(PlainTextComponentSerializer.plainText().serialize(message));
                    builder = builder.replace("%message%", MiniMessage.miniMessage().serialize(component));
                } else {
                    builder = builder.replace("%message%", PlainTextComponentSerializer.plainText().serialize(message));
                }

                ItemStack item = source.getInventory().getItemInMainHand();
                if (format().useItemDisplay() && source.hasPermission(Perm.CHAT_ITEM) && isChatItemSyntax(message) && !item.getType().isAir()) {
                    String itemMiniMessage = createItemName(item.displayName());

                    builder = builder.replace(Pattern.quote("{i}"), itemMiniMessage);
                    builder = builder.replace("\\{item\\}", itemMiniMessage);
                }

                return builder.asComponent(source);
            } catch (ExecutionException e) {
                return new StaticGlobalChatRenderer().render(source, display, message, viewer);
            }
        }
    }

    private boolean isChatItemSyntax(@NotNull Component message) {
        String string = PlainTextComponentSerializer.plainText().serialize(message);
        return string.contains("{i}") || string.contains("{item}");
    }

    private String createItemName(@NotNull Component component) {
        String itemName = MiniMessage.miniMessage().serialize(component);

        Integer start = null;
        Integer end = null;
        for (int i = 0; i < itemName.length(); i++) {
            if (itemName.charAt(i) == '<') start = i;
            if (itemName.charAt(i) == '>') end = i;

            if (start != null && end != null) break;
        }

        if (start == null || end == null) return itemName;

        String between = itemName.substring(start + 1, end);
        if (between.contains("color")) return itemName + "</color>";
        if (between.contains("gradient")) return itemName + "</gradient>";

        return itemName + "</" + between + ">";
    }
}
