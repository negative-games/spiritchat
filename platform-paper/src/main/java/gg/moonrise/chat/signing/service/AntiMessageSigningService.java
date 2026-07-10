package gg.moonrise.chat.signing.service;

import gg.moonrise.chat.config.section.chat.AntiMessageSigningSettings;
import gg.moonrise.chat.config.ConfigService;
import gg.moonrise.chat.signing.packet.AntiMessageSigningPacketHandler;
import gg.moonrise.chat.signing.packet.MessageSigningOptions;
import gg.moonrise.chat.signing.packet.PlayerChannelResolver;
import gg.moonrise.chat.util.PlatformTasks;
import gg.moonrise.engine.state.Reloadable;
import gg.moonrise.moss.spring.Disableable;
import gg.moonrise.moss.spring.Enableable;
import gg.moonrise.moss.spring.SpringComponent;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@SpringComponent
@RequiredArgsConstructor
@Slf4j(topic = "SpiritChat")
public class AntiMessageSigningService implements Listener, Enableable, Disableable, Reloadable {

    private static final Key LISTENER_KEY = Key.key("spiritchat", "anti_message_signing");
    private static final String UNSIGNED_CHAT_HELP = """
            SpiritChat can make formatted outgoing chat unreportable and can advertise preventsChatReports=true for clients that understand report-prevention metadata. To allow clients that do not send chat signatures at all, set enforce-secure-profile=false in server.properties.""";
    private static final String SECURE_PROFILE_WARNING = """
            The server is still enforcing secure profiles. Clients without profile public keys can be blocked before SpiritChat can rewrite outgoing chat. Set enforce-secure-profile=false in server.properties for unsigned-chat compatibility.""";

    private final ConfigService config;

    private final Set<Channel> channels = ConcurrentHashMap.newKeySet();
    private final AtomicInteger lifecycleGeneration = new AtomicInteger();
    private Object listener;
    private volatile MessageSigningOptions activeOptions;
    private volatile boolean secureProfileEnforced;

    @Override
    public void onEnable() {
        reload();
    }

    @Override
    public void onDisable() {
        unregister();
    }

    @Override
    public void reload() {
        unregister();

        AntiMessageSigningSettings settings = config.get().getAntiMessageSigningSettings();
        if (!settings.isEnabled()) {
            activeOptions = null;
            secureProfileEnforced = false;
            log.info("Chat-signing compatibility is disabled.");
            return;
        }

        if (!settings.hasAnyActiveFeature()) {
            activeOptions = null;
            secureProfileEnforced = false;
            log.info("Chat-signing compatibility is enabled, but all compatibility features are disabled.");
            return;
        }

        CompatibleSigningFeatures features = compatibleFeatures(settings);
        if (!features.any()) {
            activeOptions = null;
            secureProfileEnforced = false;
            log.warn("Chat-signing compatibility was not enabled because no configured feature is compatible with this server build.");
            return;
        }

        try {
            MessageSigningOptions options = register(settings, features);
            activeOptions = options;
            secureProfileEnforced = isSecureProfileEnforced();
            log.info(
                    "Chat-signing compatibility enabled: rewrite-player-chat={}, send-prevents-chat-reports-to-client={}, claim-secure-chat-enforced={}, bedrock-only={}.",
                    options.rewritePlayerChat(),
                    options.sendPreventsChatReportsToClient(),
                    options.claimSecureChatEnforced(),
                    options.bedrockOnly()
            );
            log.info(UNSIGNED_CHAT_HELP);
            if (secureProfileEnforced) {
                log.warn(SECURE_PROFILE_WARNING);
            }
        } catch (ReflectiveOperationException exception) {
            activeOptions = null;
            secureProfileEnforced = false;
            log.error("Failed to enable chat-signing compatibility.", exception);
        }
    }

    private CompatibleSigningFeatures compatibleFeatures(AntiMessageSigningSettings settings) {
        boolean rewritePlayerChat = false;
        boolean sendPreventsChatReportsToClient = false;
        boolean claimSecureChatEnforced = false;

        if (settings.isRewritePlayerChat()) {
            if (AntiMessageSigningPacketHandler.isChatRewriteSupported()) {
                rewritePlayerChat = true;
            } else {
                log.warn("Configured chat-signing feature rewrite-player-chat is not compatible with this server's chat packet implementation.");
            }
        }

        if (settings.isSendPreventsChatReportsToClient()) {
            if (AntiMessageSigningPacketHandler.isStatusRewriteSupported()) {
                sendPreventsChatReportsToClient = true;
            } else {
                log.warn("Configured chat-signing feature send-prevents-chat-reports-to-client is not compatible with this server's status packet implementation.");
            }
        }

        if (settings.isClaimSecureChatEnforced()) {
            if (AntiMessageSigningPacketHandler.isLoginRewriteSupported()) {
                claimSecureChatEnforced = true;
            } else {
                log.warn("Configured chat-signing feature claim-secure-chat-enforced is not compatible with this server's login packet implementation.");
            }
        }

        return new CompatibleSigningFeatures(
                rewritePlayerChat,
                sendPreventsChatReportsToClient,
                claimSecureChatEnforced
        );
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        MessageSigningOptions options = activeOptions;
        if (options == null) return;

        PlatformTasks.run(event.getPlayer(), () -> PlayerChannelResolver.resolve(event.getPlayer())
                .ifPresent(channel -> injectHandler(channel, options)));

        if (secureProfileEnforced) {
            PlatformTasks.run(event.getPlayer(), () -> {
                if (event.getPlayer().isOnline()) {
                    config.send(event.getPlayer(), config.messages().getGeneral().getSecureProfileEnforced());
                }
            });
        }
    }

    private MessageSigningOptions register(AntiMessageSigningSettings settings, CompatibleSigningFeatures features) throws ReflectiveOperationException {
        Class<?> holder = Class.forName("io.papermc.paper.network.ChannelInitializeListenerHolder");
        Class<?> listenerType = Class.forName("io.papermc.paper.network.ChannelInitializeListener");
        Method addListener = holder.getMethod("addListener", Key.class, listenerType);
        int generation = lifecycleGeneration.incrementAndGet();
        MessageSigningOptions options = features.options(settings.isBedrockOnly(), generation);

        listener = Proxy.newProxyInstance(
                listenerType.getClassLoader(),
                new Class<?>[]{listenerType},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return handleObjectMethod(proxy, method, args);
                    }

                    if (args != null && args.length == 1 && args[0] instanceof Channel channel) {
                        try {
                            injectHandler(channel, options);
                        } catch (RuntimeException exception) {
                            log.warn("Failed to inject anti-message-signing packet handler for a player connection.", exception);
                        }
                    }
                    return null;
                }
        );

        addListener.invoke(null, LISTENER_KEY, listener);
        injectOnlinePlayers(options);
        return options;
    }

    private void injectOnlinePlayers(MessageSigningOptions options) {
        PlatformTasks.runGlobal(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerChannelResolver.resolve(player)
                        .ifPresent(channel -> injectHandler(channel, options));
            }
        });
    }

    private void unregister() {
        int invalidBeforeGeneration = lifecycleGeneration.incrementAndGet();
        try {
            Class<?> holder = Class.forName("io.papermc.paper.network.ChannelInitializeListenerHolder");
            Method hasListener = holder.getMethod("hasListener", Key.class);
            Method removeListener = holder.getMethod("removeListener", Key.class);

            if ((boolean) hasListener.invoke(null, LISTENER_KEY)) {
                removeListener.invoke(null, LISTENER_KEY);
            }
        } catch (ReflectiveOperationException exception) {
            log.debug("Anti-message-signing listener was not registered.", exception);
        } finally {
            removeInjectedHandlers(invalidBeforeGeneration);
            listener = null;
            activeOptions = null;
        }
    }

    private void injectHandler(Channel channel, MessageSigningOptions options) {
        if (options.generation() != lifecycleGeneration.get() || !channel.isOpen()) {
            return;
        }

        if (!channel.eventLoop().inEventLoop()) {
            channel.eventLoop().execute(() -> injectHandler(channel, options, 2));
            return;
        }

        injectHandler(channel, options, 2);
    }

    private void injectHandler(Channel channel, MessageSigningOptions options, int attemptsRemaining) {
        if (options.generation() != lifecycleGeneration.get() || !channel.isOpen()) {
            return;
        }

        if (channel.pipeline().get(AntiMessageSigningPacketHandler.HANDLER_NAME) != null) {
            return;
        }

        if (channel.pipeline().get("packet_handler") == null) {
            if (attemptsRemaining > 0) {
                channel.eventLoop().execute(() -> injectHandler(channel, options, attemptsRemaining - 1));
                return;
            }

            log.debug("Skipping anti-message-signing packet handler injection because packet_handler is not present.");
            return;
        }

        channel.pipeline().addAfter(
                "packet_handler",
                AntiMessageSigningPacketHandler.HANDLER_NAME,
                new AntiMessageSigningPacketHandler(options)
        );
        channels.add(channel);
        channel.closeFuture().addListener(future -> channels.remove(channel));
    }

    private void removeInjectedHandlers(int invalidBeforeGeneration) {
        for (Channel channel : channels) {
            try {
                channel.eventLoop().execute(() -> {
                    if (shouldRemoveHandler(channel, invalidBeforeGeneration)) {
                        channel.pipeline().remove(AntiMessageSigningPacketHandler.HANDLER_NAME);
                    }
                });
            } catch (RuntimeException exception) {
                log.debug("Failed to remove anti-message-signing packet handler from a channel.", exception);
            }
        }
        channels.clear();
    }

    private boolean shouldRemoveHandler(Channel channel, int invalidBeforeGeneration) {
        if (!channel.isOpen()) return false;

        if (channel.pipeline().get(AntiMessageSigningPacketHandler.HANDLER_NAME) instanceof AntiMessageSigningPacketHandler handler) {
            return handler.generation() < invalidBeforeGeneration;
        }
        return false;
    }

    private Object handleObjectMethod(Object proxy, Method method, Object[] args) {
        return switch (method.getName()) {
            case "toString" -> "SpiritChat anti-message-signing channel listener";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> args != null && args.length == 1 && proxy == args[0];
            default -> null;
        };
    }

    private boolean isSecureProfileEnforced() {
        try {
            Object craftServer = Bukkit.getServer();
            Object minecraftServer = craftServer.getClass().getMethod("getServer").invoke(craftServer);
            return (boolean) minecraftServer.getClass().getMethod("enforceSecureProfile").invoke(minecraftServer);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            log.debug("Could not determine enforce-secure-profile state.", exception);
            return false;
        }
    }

    private record CompatibleSigningFeatures(
            boolean rewritePlayerChat,
            boolean sendPreventsChatReportsToClient,
            boolean claimSecureChatEnforced
    ) {

        private boolean any() {
            return rewritePlayerChat || sendPreventsChatReportsToClient || claimSecureChatEnforced;
        }

        private MessageSigningOptions options(boolean bedrockOnly, int generation) {
            return new MessageSigningOptions(
                    rewritePlayerChat,
                    claimSecureChatEnforced,
                    sendPreventsChatReportsToClient,
                    bedrockOnly,
                    generation
            );
        }
    }
}
