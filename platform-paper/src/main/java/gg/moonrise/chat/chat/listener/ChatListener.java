package gg.moonrise.chat.chat.listener;

import gg.moonrise.chat.chat.format.ChatFormatService;
import gg.moonrise.chat.chat.format.FormattedChatMessage;
import gg.moonrise.chat.chat.format.GroupChatRenderer;
import gg.moonrise.chat.chat.format.StaticChatRenderer;
import gg.moonrise.chat.chat.integration.LuckPermsService;
import gg.moonrise.chat.chat.item.ChatItemService;
import gg.moonrise.chat.config.Config;
import gg.moonrise.chat.config.ConfigService;
import gg.moonrise.chat.config.section.chat.GroupChatSettings;
import gg.moonrise.chat.config.section.chat.StaticChatSettings;
import gg.moonrise.chat.logging.service.ChatLogService;
import gg.moonrise.chat.mention.service.MentionService;
import gg.moonrise.engine.message.util.MiniMessageUtil;
import gg.moonrise.engine.state.Reloadable;
import gg.moonrise.moss.spring.Disableable;
import gg.moonrise.moss.spring.Enableable;
import gg.moonrise.moss.spring.SpringComponent;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

@Slf4j
@SpringComponent
@RequiredArgsConstructor
public class ChatListener implements Listener, Enableable, Disableable, Reloadable {

    private static volatile ChatRenderer GLOBAL_RENDERER;

    private volatile String rendererDescription = "disabled";

    private final ConfigService config;
    private final LuckPermsService luckPermsService;
    private final MentionService mentionService;
    private final ChatItemService chatItemService;
    private final ChatFormatService chatFormatService;
    private final ChatLogService chatLogService;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAsyncChatFormat(AsyncChatEvent event) {
        ChatRenderer renderer = GLOBAL_RENDERER;
        if (renderer == null) return;

        FormattedChatMessage message = chatFormatService.prepareMessage(event.getPlayer(), event.message());
        event.viewers().remove(Bukkit.getConsoleSender());
        Bukkit.getLogger().info("[Chat] " + event.getPlayer().getName() + ": " + consoleLine(MiniMessageUtil.componentToPlainText(event.message())));
        event.message(message.component());
        chatItemService.prepareSnapshot(event.getPlayer(), message.input());

        event.renderer(renderer);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncChatComplete(AsyncChatEvent event) {
        if (event.isCancelled()) {
            chatItemService.clearSnapshot(event.getPlayer());
            return;
        }

        String input = GLOBAL_RENDERER == null ? null : MiniMessageUtil.componentToPlainText(event.message());
        mentionService.notifyMentionedPlayers(event.getPlayer(), input, event.viewers());
        chatLogService.log(event.getPlayer(), event.message());
        chatItemService.clearSnapshotAfterRender(event.getPlayer());
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
        setGlobalRenderer(null);
    }

    @Override
    public void reload() {
        chatItemService.clearCache();

        Config config = this.config.get();
        StaticChatSettings staticChatSettings = config.getStaticChatSettings();
        if (staticChatSettings.isEnabled()) {
            setGlobalRenderer(new StaticChatRenderer(chatFormatService, this.config));
            rendererDescription = "static format loaded: " + staticChatSettings.effectiveFormat();
            return;
        }

        GroupChatSettings groupChatSettings = config.getGroupChatSettings();
        if (groupChatSettings.isEnabled()) {
            if (!luckPermsService.isAvailable()) {
                log.error("LuckPerms not found! Cannot initialize Group Chat Renderer.");
                setGlobalRenderer(null);
                rendererDescription = "disabled because LuckPerms is unavailable";
                return;
            }

            setGlobalRenderer(new GroupChatRenderer(chatFormatService, this.config, luckPermsService));
            rendererDescription = "group formats loaded: " + groupChatSettings.effectiveFormats().size();
            return;
        }

        setGlobalRenderer(null);
        rendererDescription = "disabled because static and group chat formatting are off";
        log.error("Could not initialize a Chat Renderer. Global chat messages will not be formatted.");
    }

    public String rendererDescription() {
        return rendererDescription;
    }

    private static void setGlobalRenderer(ChatRenderer renderer) {
        GLOBAL_RENDERER = renderer;
    }
}
