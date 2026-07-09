package gg.moonrise.chat.controller;

import gg.moonrise.chat.config.Config;
import gg.moonrise.chat.service.ConfigService;
import gg.moonrise.chat.config.section.chat.ChatItemSettings;
import gg.moonrise.chat.config.section.chat.GroupChatSettings;
import gg.moonrise.chat.config.section.chat.StaticChatSettings;
import gg.moonrise.chat.controller.format.GroupChatController;
import gg.moonrise.chat.controller.format.StaticChatController;
import gg.moonrise.chat.service.LuckPermsService;
import gg.moonrise.engine.message.util.MiniMessageUtil;
import gg.moonrise.engine.state.Reloadable;
import gg.moonrise.moss.spring.Disableable;
import gg.moonrise.moss.spring.Enableable;
import gg.moonrise.moss.spring.SpringComponent;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

@Slf4j
@SpringComponent
public class ChatController implements Listener, Enableable, Disableable, Reloadable {

    private static volatile ChatRenderer GLOBAL_RENDERER;

    private final ConfigService config;
    private final LuckPermsService luckPermsService;

    public ChatController(ConfigService config, LuckPermsService luckPermsService) {
        this.config = config;
        this.luckPermsService = luckPermsService;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncChat(AsyncChatEvent event) {
        ChatRenderer renderer = GLOBAL_RENDERER;
        if (event.isCancelled() || renderer == null) return;

        event.renderer(renderer);
    }

    public Component applyFormat(Player player, String format, Component original) {
        String input = MiniMessageUtil.componentToPlainText(original);
        Component message = formatMessage(player, input);
        Component formattedMessage = formatChatItemMessage(player, input, message);

        String rendered = format
                .replace("{player}", player.getName())
                .replace("{message}", "<message>");

        return MiniMessageUtil.fromText(
                player,
                rendered,
                Placeholder.component("message", formattedMessage)
        );
    }

    public Component formatChatItemMessage(Player player, String input, Component component) {
        ChatItemSettings settings = config.get().getChatItemSettings();
        if (!settings.isEnabled()
                || !settings.containsChatItemSyntax(input)
                || !player.hasPermission(settings.getPermission())) return component;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (settings.isItemTypeBlocked(item.getType())) return component;

        for (String placeholder : settings.getPlaceholders()) {
            if (placeholder == null || placeholder.isEmpty()) continue;

            component = component.replaceText(TextReplacementConfig.builder()
                    .matchLiteral(placeholder)
                    .replacement(item.effectiveName().hoverEvent(item.asHoverEvent()))
                    .build());
        }

        return component;
    }

    public Component formatMessage(Player player, String message) {
        if (!player.hasPermission("spiritchat.chat-colors")) {
            return Component.text(MiniMessageUtil.INSTANCE.stripTags(message));
        }

        return MiniMessageUtil.legacyToComponent(message);
    }

    @Override
    public void onEnable() {
        reload();
    }

    @Override
    public void onDisable() {
        ChatController.setGlobalRenderer(null);
    }

    @Override
    public void reload() {
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
