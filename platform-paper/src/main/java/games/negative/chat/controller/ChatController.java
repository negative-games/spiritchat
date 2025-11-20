package games.negative.chat.controller;

import games.negative.alumina.message.Message;
import games.negative.chat.config.Config;
import games.negative.chat.service.ConfigService;
import games.negative.chat.config.section.chat.ChatItemSettings;
import games.negative.chat.config.section.chat.GroupChatSettings;
import games.negative.chat.config.section.chat.StaticChatSettings;
import games.negative.chat.controller.format.GroupChatController;
import games.negative.chat.controller.format.StaticChatController;
import games.negative.chat.service.LuckPermsService;
import games.negative.chat.spring.Reloadable;
import games.negative.chat.spring.SpringComponent;
import games.negative.chat.util.ChatUtil;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Pattern;

@Slf4j
@SpringComponent
public class ChatController implements Listener, Reloadable {

    private static ChatRenderer GLOBAL_RENDERER;

    private final ConfigService config;
    private final LuckPermsService luckPermsService;

    public ChatController(ConfigService config, LuckPermsService luckPermsService) {
        this.config = config;
        this.luckPermsService = luckPermsService;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncChat(AsyncChatEvent event) {
        if (event.isCancelled() || GLOBAL_RENDERER == null) return;

        event.renderer(GLOBAL_RENDERER);
    }

    public Component applyFormat(Player player, Message format, Component original) {
        String input = formatMessage(player, original);

        Message.Builder builder = format.create(ChatUtil.MINIMESSAGE);
        builder.replace(Pattern.quote("{player}"), player.getName());
        builder.replace(Pattern.quote("{message}"), input);

        formatChatItemMessage(player, input, builder);

        return builder.asComponent(player);
    }

    public void formatChatItemMessage(Player player, String input, Message.Builder builder) {
        ChatItemSettings settings = config.get().getChatItemSettings();
        if (!settings.isEnabled()
                || !settings.containsChatItemSyntax(input)
                || !player.hasPermission(settings.getPermission())) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (settings.isItemTypeBlocked(item.getType())) return;

        for (String placeholder : settings.getPlaceholders()) {
            builder.replace(Pattern.quote(placeholder), "<white>%spiritchat-item%</white>");
        }

        Component name = item.effectiveName().hoverEvent(item.asHoverEvent());
        builder.replace("%spiritchat-item%", name);
    }

    public String formatMessage(Player player, Component message) {
        String text;
        if (!player.hasPermission("spiritchat.chat-colors")) {
            text = ChatUtil.MINIMESSAGE.stripTags(ChatUtil.PLAIN_SERIALIZER.serialize(message));
        } else {
            Component component = ChatUtil.LEGACY_SERIALIZER.deserialize(ChatUtil.PLAIN_SERIALIZER.serialize(message));
            text = ChatUtil.MINIMESSAGE.serialize(component);
        }

        return text;
    }

    @Override
    public void onReload() {
        Config config = this.config.get();
        StaticChatSettings staticChatSettings = config.getStaticChatSettings();
        if (staticChatSettings.isEnabled()) {
            ChatController.setGlobalRenderer(new StaticChatController(this, staticChatSettings));
            log.info("Successfully initialized Static Chat Renderer.");
            return;
        }

        GroupChatSettings groupChatSettings = config.getGroupChatSettings();
        if (groupChatSettings.isEnabled()) {
            if (luckPermsService.luckPerms().isEmpty()) {
                log.error("LuckPerms not found! Cannot initialize Group Chat Renderer.");
                ChatController.setGlobalRenderer(null);
                return;
            }

            ChatController.setGlobalRenderer(new GroupChatController(this, groupChatSettings, luckPermsService));
            log.info("Successfully initialized Group Chat Renderer.");
            return;
        }

        ChatController.setGlobalRenderer(null);
        log.error("Could not initialize a Chat Renderer. Global chat messages will not be formatted.");
    }

    public static void setGlobalRenderer(ChatRenderer renderer) {
        GLOBAL_RENDERER = renderer;
    }
}
