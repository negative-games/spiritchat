package gg.moonrise.chat.packet;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;

@Slf4j
public final class AntiMessageSigningPacketHandler extends ChannelDuplexHandler {

    public static final String HANDLER_NAME = "spiritchat_anti_message_signing";

    private static final String PLAYER_CHAT_PACKET = "net.minecraft.network.protocol.game.ClientboundPlayerChatPacket";
    private static final String LOGIN_PACKET = "net.minecraft.network.protocol.game.ClientboundLoginPacket";
    private static final String SYSTEM_CHAT_PACKET = "net.minecraft.network.protocol.game.ClientboundSystemChatPacket";
    private static final String COMPONENT = "net.minecraft.network.chat.Component";

    private final boolean bedrockOnly;
    private final boolean claimSecureChatEnforced;
    private Reflection reflection;
    private boolean rewriteFailureLogged = false;

    public AntiMessageSigningPacketHandler(boolean bedrockOnly, boolean claimSecureChatEnforced) {
        this.bedrockOnly = bedrockOnly;
        this.claimSecureChatEnforced = claimSecureChatEnforced;
    }

    public static boolean isSupported() {
        try {
            Reflection.load();
            return true;
        } catch (ReflectiveOperationException exception) {
            log.debug("Anti-message-signing packet rewrite is not supported by this server.", exception);
            return false;
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (PLAYER_CHAT_PACKET.equals(msg.getClass().getName())) {
            rewritePlayerChat(ctx, msg, promise);
            return;
        }

        if (claimSecureChatEnforced && LOGIN_PACKET.equals(msg.getClass().getName())) {
            rewriteLogin(ctx, msg, promise);
            return;
        }

        super.write(ctx, msg, promise);
    }

    private void rewritePlayerChat(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        try {
            Reflection reflection = reflection();
            if (bedrockOnly && !isBedrockPlayer((UUID) reflection.sender.invoke(msg))) {
                super.write(ctx, msg, promise);
                return;
            }

            Object content = Objects.requireNonNullElseGet(
                    reflection.unsignedContent.invoke(msg),
                    () -> literalContent(reflection, msg)
            );
            Object decoratedContent = reflection.decorate.invoke(reflection.chatType.invoke(msg), content);
            Object systemPacket = reflection.systemChatConstructor.newInstance(decoratedContent, false);

            ctx.write(systemPacket, promise);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            failOpen(ctx, msg, promise, "Failed to rewrite a signed chat packet. Sending the original packet instead.", exception);
        }
    }

    private void rewriteLogin(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        try {
            Reflection reflection = reflection();
            Object rewritten = reflection.loginConstructor.newInstance(
                    reflection.playerId.invoke(msg),
                    reflection.hardcore.invoke(msg),
                    reflection.levels.invoke(msg),
                    reflection.maxPlayers.invoke(msg),
                    reflection.chunkRadius.invoke(msg),
                    reflection.simulationDistance.invoke(msg),
                    reflection.reducedDebugInfo.invoke(msg),
                    reflection.showDeathScreen.invoke(msg),
                    reflection.doLimitedCrafting.invoke(msg),
                    reflection.commonPlayerSpawnInfo.invoke(msg),
                    reflection.onlineMode.invoke(msg),
                    true
            );
            ctx.write(rewritten, promise);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            failOpen(ctx, msg, promise, "Failed to rewrite the login packet. Sending the original packet instead.", exception);
        }
    }

    private void failOpen(ChannelHandlerContext ctx, Object msg, ChannelPromise promise, String message, Exception exception) throws Exception {
        if (!rewriteFailureLogged) {
            log.warn(message, exception);
            rewriteFailureLogged = true;
        }
        super.write(ctx, msg, promise);
    }

    private Object literalContent(Reflection reflection, Object msg) {
        try {
            Object body = reflection.body.invoke(msg);
            String content = (String) reflection.bodyContent.invoke(body);
            return reflection.literal.invoke(null, content);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to read signed chat packet body.", exception);
        }
    }

    private Reflection reflection() throws ReflectiveOperationException {
        if (reflection == null) {
            reflection = Reflection.load();
        }

        return reflection;
    }

    private boolean isBedrockPlayer(UUID uuid) {
        return uuid.version() == 0;
    }

    private record Reflection(
            Method sender,
            Method unsignedContent,
            Method body,
            Method bodyContent,
            Method chatType,
            Method decorate,
            Method literal,
            Constructor<?> systemChatConstructor,
            Constructor<?> loginConstructor,
            Method playerId,
            Method hardcore,
            Method levels,
            Method maxPlayers,
            Method chunkRadius,
            Method simulationDistance,
            Method reducedDebugInfo,
            Method showDeathScreen,
            Method doLimitedCrafting,
            Method commonPlayerSpawnInfo,
            Method onlineMode
    ) {

        private static Reflection load() throws ReflectiveOperationException {
            Class<?> playerChatPacket = Class.forName(PLAYER_CHAT_PACKET);
            Class<?> loginPacket = Class.forName(LOGIN_PACKET);
            Class<?> systemChatPacket = Class.forName(SYSTEM_CHAT_PACKET);
            Class<?> component = Class.forName(COMPONENT);

            Method chatType = playerChatPacket.getMethod("chatType");
            Class<?> boundChatType = chatType.getReturnType();

            Method body = playerChatPacket.getMethod("body");
            Class<?> bodyType = body.getReturnType();

            Method levels = loginPacket.getMethod("levels");
            Method commonPlayerSpawnInfo = loginPacket.getMethod("commonPlayerSpawnInfo");

            return new Reflection(
                    playerChatPacket.getMethod("sender"),
                    playerChatPacket.getMethod("unsignedContent"),
                    body,
                    bodyType.getMethod("content"),
                    chatType,
                    boundChatType.getMethod("decorate", component),
                    component.getMethod("literal", String.class),
                    systemChatPacket.getConstructor(component, boolean.class),
                    loginPacket.getConstructor(
                            int.class,
                            boolean.class,
                            levels.getReturnType(),
                            int.class,
                            int.class,
                            int.class,
                            boolean.class,
                            boolean.class,
                            boolean.class,
                            commonPlayerSpawnInfo.getReturnType(),
                            boolean.class,
                            boolean.class
                    ),
                    loginPacket.getMethod("playerId"),
                    loginPacket.getMethod("hardcore"),
                    levels,
                    loginPacket.getMethod("maxPlayers"),
                    loginPacket.getMethod("chunkRadius"),
                    loginPacket.getMethod("simulationDistance"),
                    loginPacket.getMethod("reducedDebugInfo"),
                    loginPacket.getMethod("showDeathScreen"),
                    loginPacket.getMethod("doLimitedCrafting"),
                    commonPlayerSpawnInfo,
                    loginPacket.getMethod("onlineMode")
            );
        }
    }
}
