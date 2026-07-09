package gg.moonrise.chat.service;

import gg.moonrise.chat.config.section.chat.AntiMessageSigningSettings;
import gg.moonrise.chat.packet.AntiMessageSigningPacketHandler;
import gg.moonrise.engine.state.Reloadable;
import gg.moonrise.moss.spring.Disableable;
import gg.moonrise.moss.spring.Enableable;
import gg.moonrise.moss.spring.SpringComponent;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.key.Key;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SpringComponent
@RequiredArgsConstructor
@Slf4j
public class AntiMessageSigningService implements Enableable, Disableable, Reloadable {

    private static final Key LISTENER_KEY = Key.key("spiritchat", "anti_message_signing");
    private static final String UNSIGNED_CHAT_HELP = """
            If players without profile public keys still trigger Paper warnings like 'Failed to update secure chat state', set enforce-secure-profile=false in server.properties. SpiritChat's anti-message-signing defaults are already permissive for unsigned chat: enabled=true and claim-secure-chat-enforced=false.""";

    private final ConfigService config;

    private final Set<Channel> channels = ConcurrentHashMap.newKeySet();
    private Object listener;

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
            log.info("Anti-message-signing packet rewrite is disabled.");
            return;
        }

        if (!AntiMessageSigningPacketHandler.isSupported()) {
            log.warn("Anti-message-signing packet rewrite is not compatible with this server's chat packet implementation.");
            return;
        }

        try {
            register(settings);
            log.info("Anti-message-signing packet rewrite enabled.");
            log.info(UNSIGNED_CHAT_HELP);
        } catch (ReflectiveOperationException exception) {
            log.error("Failed to enable anti-message-signing packet rewrite.", exception);
        }
    }

    private void register(AntiMessageSigningSettings settings) throws ReflectiveOperationException {
        Class<?> holder = Class.forName("io.papermc.paper.network.ChannelInitializeListenerHolder");
        Class<?> listenerType = Class.forName("io.papermc.paper.network.ChannelInitializeListener");
        Method addListener = holder.getMethod("addListener", Key.class, listenerType);

        listener = Proxy.newProxyInstance(
                listenerType.getClassLoader(),
                new Class<?>[]{listenerType},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return handleObjectMethod(proxy, method, args);
                    }

                    if (args != null && args.length == 1 && args[0] instanceof Channel channel) {
                        try {
                            injectHandler(channel, settings);
                        } catch (RuntimeException exception) {
                            log.warn("Failed to inject anti-message-signing packet handler for a player connection.", exception);
                        }
                    }
                    return null;
                }
        );

        addListener.invoke(null, LISTENER_KEY, listener);
    }

    private void unregister() {
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
            removeInjectedHandlers();
            listener = null;
        }
    }

    private void injectHandler(Channel channel, AntiMessageSigningSettings settings) {
        if (channel.pipeline().get(AntiMessageSigningPacketHandler.HANDLER_NAME) != null) {
            return;
        }

        if (channel.pipeline().get("packet_handler") == null) {
            log.debug("Skipping anti-message-signing packet handler injection because packet_handler is not present yet.");
            return;
        }

        channel.pipeline().addAfter(
                "packet_handler",
                AntiMessageSigningPacketHandler.HANDLER_NAME,
                new AntiMessageSigningPacketHandler(settings.isBedrockOnly(), settings.isClaimSecureChatEnforced())
        );
        channels.add(channel);
        channel.closeFuture().addListener(future -> channels.remove(channel));
    }

    private void removeInjectedHandlers() {
        for (Channel channel : channels) {
            if (channel.pipeline().get(AntiMessageSigningPacketHandler.HANDLER_NAME) == null) {
                continue;
            }

            try {
                channel.eventLoop().execute(() -> {
                    if (channel.pipeline().get(AntiMessageSigningPacketHandler.HANDLER_NAME) != null) {
                        channel.pipeline().remove(AntiMessageSigningPacketHandler.HANDLER_NAME);
                    }
                });
            } catch (RuntimeException exception) {
                log.debug("Failed to remove anti-message-signing packet handler from a channel.", exception);
            }
        }
        channels.clear();
    }

    private Object handleObjectMethod(Object proxy, Method method, Object[] args) {
        return switch (method.getName()) {
            case "toString" -> "SpiritChat anti-message-signing channel listener";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> args != null && args.length == 1 && proxy == args[0];
            default -> null;
        };
    }
}
