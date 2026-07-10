package gg.moonrise.chat.signing.packet;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

final class ChatPacketReflection {

    static final String PLAYER_CHAT_PACKET = "net.minecraft.network.protocol.game.ClientboundPlayerChatPacket";

    private static final String SYSTEM_CHAT_PACKET = "net.minecraft.network.protocol.game.ClientboundSystemChatPacket";
    private static final String COMPONENT = "net.minecraft.network.chat.Component";

    private final Method sender;
    private final Method unsignedContent;
    private final Method body;
    private final Method bodyContent;
    private final Method chatType;
    private final Method decorate;
    private final Method literal;
    private final Constructor<?> systemChatConstructor;

    private ChatPacketReflection(
            Method sender,
            Method unsignedContent,
            Method body,
            Method bodyContent,
            Method chatType,
            Method decorate,
            Method literal,
            Constructor<?> systemChatConstructor
    ) {
        this.sender = sender;
        this.unsignedContent = unsignedContent;
        this.body = body;
        this.bodyContent = bodyContent;
        this.chatType = chatType;
        this.decorate = decorate;
        this.literal = literal;
        this.systemChatConstructor = systemChatConstructor;
    }

    static ChatPacketReflection load() throws ReflectiveOperationException {
        Class<?> playerChatPacket = Class.forName(PLAYER_CHAT_PACKET);
        Class<?> systemChatPacket = Class.forName(SYSTEM_CHAT_PACKET);
        Class<?> component = Class.forName(COMPONENT);

        Method chatType = playerChatPacket.getMethod("chatType");
        Class<?> boundChatType = chatType.getReturnType();

        Method body = playerChatPacket.getMethod("body");
        Class<?> bodyType = body.getReturnType();

        return new ChatPacketReflection(
                playerChatPacket.getMethod("sender"),
                playerChatPacket.getMethod("unsignedContent"),
                body,
                bodyType.getMethod("content"),
                chatType,
                boundChatType.getMethod("decorate", component),
                component.getMethod("literal", String.class),
                systemChatPacket.getConstructor(component, boolean.class)
        );
    }

    boolean isBedrockSender(Object packet) throws ReflectiveOperationException {
        return ((UUID) sender.invoke(packet)).version() == 0;
    }

    Object rewrite(Object packet) throws ReflectiveOperationException {
        Object content = content(packet);
        Object decoratedContent = decorate.invoke(chatType.invoke(packet), content);
        return systemChatConstructor.newInstance(decoratedContent, false);
    }

    private Object content(Object packet) throws ReflectiveOperationException {
        Object content = unsignedContent.invoke(packet);
        if (content instanceof Optional<?> optional) {
            return optional.isPresent() ? optional.get() : literalContent(packet);
        }

        return Objects.requireNonNullElseGet(content, () -> literalContent(packet));
    }

    private Object literalContent(Object packet) {
        try {
            Object chatBody = body.invoke(packet);
            return literal.invoke(null, (String) bodyContent.invoke(chatBody));
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to read signed chat packet body.", exception);
        }
    }
}
