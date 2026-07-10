package gg.moonrise.chat.controller;

import gg.moonrise.chat.config.Config;
import gg.moonrise.chat.service.ConfigService;
import gg.moonrise.chat.config.section.chat.GroupChatSettings;
import gg.moonrise.chat.config.section.chat.StaticChatSettings;
import gg.moonrise.chat.controller.format.GroupChatController;
import gg.moonrise.chat.controller.format.StaticChatController;
import gg.moonrise.chat.service.ChatFormatService;
import gg.moonrise.chat.service.ChatItemService;
import gg.moonrise.chat.service.ChatLogService;
import gg.moonrise.chat.service.FormattedChatMessage;
import gg.moonrise.chat.service.LuckPermsService;
import gg.moonrise.chat.service.MentionService;
import gg.moonrise.engine.message.util.MiniMessageUtil;
import gg.moonrise.engine.state.Reloadable;
import gg.moonrise.moss.spring.Disableable;
import gg.moonrise.moss.spring.Enableable;
import gg.moonrise.moss.spring.SpringComponent;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@SpringComponent
public class ChatController implements Listener, Enableable, Disableable, Reloadable {

    private static volatile ChatRenderer GLOBAL_RENDERER;
    private final Map<AsyncChatEvent, String> pendingChatInputs = new ConcurrentHashMap<>();

    private final ConfigService config;
    private final LuckPermsService luckPermsService;
    private final MentionService mentionService;
    private final ChatItemService chatItemService;
    private final ChatFormatService chatFormatService;
    private final ChatLogService chatLogService;

    public ChatController(ConfigService config, LuckPermsService luckPermsService, MentionService mentionService, ChatItemService chatItemService, ChatFormatService chatFormatService, ChatLogService chatLogService) {
        this.config = config;
        this.luckPermsService = luckPermsService;
        this.mentionService = mentionService;
        this.chatItemService = chatItemService;
        this.chatFormatService = chatFormatService;
        this.chatLogService = chatLogService;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAsyncChatFormat(AsyncChatEvent event) {
        ChatRenderer renderer = GLOBAL_RENDERER;
        if (renderer == null) return;

        FormattedChatMessage message = chatFormatService.prepareMessage(event.getPlayer(), event.message());
        event.viewers().remove(Bukkit.getConsoleSender());
        Bukkit.getLogger().info("[Chat] " + event.getPlayer().getName() + ": " + consoleLine(MiniMessageUtil.componentToPlainText(event.message())));
        pendingChatInputs.put(event, message.input());
        event.message(message.component());
        if (chatItemService.shouldShowcaseItem(message.input())) {
            chatItemService.prepareSnapshot(event.getPlayer(), message.input());
        } else {
            chatItemService.clearSnapshot(event.getPlayer());
        }

        event.renderer(renderer);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncChatComplete(AsyncChatEvent event) {
        if (event.isCancelled()) {
            pendingChatInputs.remove(event);
            chatItemService.clearSnapshot(event.getPlayer());
            return;
        }

        String input = pendingChatInputs.remove(event);
        mentionService.notifyMentionedPlayers(event.getPlayer(), input, event.viewers());
        chatLogService.log(event.getPlayer(), event.message());
        chatItemService.clearSnapshotAfterRender(event.getPlayer());
    }

    public ChatFormatService formatter() {
        return chatFormatService;
    }

    private String consoleLine(String message) {
        return message == null ? "" : message.replace('\n', ' ').replace('\r', ' ');
    }

    @Override
    public void onEnable() {
        reload();
    }

    @Override
    public void onDisable() {
        pendingChatInputs.clear();
        ChatController.setGlobalRenderer(null);
    }

    @Override
    public void reload() {
        chatItemService.clearCache();

        Config config = this.config.get();
        StaticChatSettings staticChatSettings = config.getStaticChatSettings();
        if (staticChatSettings.isEnabled()) {
            ChatController.setGlobalRenderer(new StaticChatController(this, staticChatSettings));
            log.info("Successfully initialized Static Chat Renderer.");
            return;
        }

        GroupChatSettings groupChatSettings = config.getGroupChatSettings();
        if (groupChatSettings.isEnabled()) {
            if (!luckPermsService.isAvailable()) {
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
