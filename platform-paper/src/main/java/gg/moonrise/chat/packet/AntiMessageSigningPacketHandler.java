package gg.moonrise.chat.packet;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class AntiMessageSigningPacketHandler extends ChannelDuplexHandler {

    public static final String HANDLER_NAME = "spiritchat_anti_message_signing";

    private final MessageSigningOptions options;
    private ChatPacketReflection chatReflection;
    private LoginPacketReflection loginReflection;
    private StatusPacketReflection statusReflection;
    private boolean rewriteFailureLogged = false;

    public AntiMessageSigningPacketHandler(MessageSigningOptions options) {
        this.options = options;
    }

    public int generation() {
        return options.generation();
    }

    public static boolean isChatRewriteSupported() {
        return isSupported(ChatPacketReflection::load, "chat packet rewrite");
    }

    public static boolean isLoginRewriteSupported() {
        return isSupported(LoginPacketReflection::load, "secure-chat login claim");
    }

    public static boolean isStatusRewriteSupported() {
        return isSupported(StatusPacketReflection::load, "report-prevention status response");
    }

    @Override
    public void write(ChannelHandlerContext context, Object packet, ChannelPromise promise) throws Exception {
        String packetType = packet.getClass().getName();

        if (options.rewritePlayerChat() && ChatPacketReflection.PLAYER_CHAT_PACKET.equals(packetType)) {
            rewritePlayerChat(context, packet, promise);
            return;
        }

        if (options.claimSecureChatEnforced() && LoginPacketReflection.LOGIN_PACKET.equals(packetType)) {
            rewriteLogin(context, packet, promise);
            return;
        }

        if (options.sendPreventsChatReportsToClient() && StatusPacketReflection.STATUS_RESPONSE_PACKET.equals(packetType)) {
            rewriteStatus(context, packet, promise);
            return;
        }

        super.write(context, packet, promise);
    }

    private void rewritePlayerChat(ChannelHandlerContext context, Object packet, ChannelPromise promise) throws Exception {
        try {
            ChatPacketReflection reflection = chatReflection();
            if (options.bedrockOnly() && !reflection.isBedrockSender(packet)) {
                super.write(context, packet, promise);
                return;
            }

            context.write(reflection.rewrite(packet), promise);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            failOpen(context, packet, promise, "Failed to rewrite a signed chat packet. Sending the original packet instead.", exception);
        }
    }

    private void rewriteLogin(ChannelHandlerContext context, Object packet, ChannelPromise promise) throws Exception {
        try {
            context.write(loginReflection().rewriteClaimingSecureChat(packet), promise);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            failOpen(context, packet, promise, "Failed to rewrite the login packet. Sending the original packet instead.", exception);
        }
    }

    private void rewriteStatus(ChannelHandlerContext context, Object packet, ChannelPromise promise) throws Exception {
        try {
            context.write(statusReflection().rewrite(context, packet), promise);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            failOpen(context, packet, promise, "Failed to rewrite the server status response. Sending the original packet instead.", exception);
        }
    }

    private void failOpen(ChannelHandlerContext context, Object packet, ChannelPromise promise, String message, Exception exception) throws Exception {
        if (!rewriteFailureLogged) {
            log.warn(message, exception);
            rewriteFailureLogged = true;
        }
        super.write(context, packet, promise);
    }

    private ChatPacketReflection chatReflection() throws ReflectiveOperationException {
        if (chatReflection == null) {
            chatReflection = ChatPacketReflection.load();
        }
        return chatReflection;
    }

    private LoginPacketReflection loginReflection() throws ReflectiveOperationException {
        if (loginReflection == null) {
            loginReflection = LoginPacketReflection.load();
        }
        return loginReflection;
    }

    private StatusPacketReflection statusReflection() throws ReflectiveOperationException {
        if (statusReflection == null) {
            statusReflection = StatusPacketReflection.load();
        }
        return statusReflection;
    }

    private static boolean isSupported(ReflectionLoader loader, String feature) {
        try {
            loader.load();
            return true;
        } catch (ReflectiveOperationException exception) {
            log.debug("SpiritChat {} is not supported by this server.", feature, exception);
            return false;
        }
    }

    @FunctionalInterface
    private interface ReflectionLoader {
        void load() throws ReflectiveOperationException;
    }
}
