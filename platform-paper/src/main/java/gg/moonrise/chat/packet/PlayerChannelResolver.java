package gg.moonrise.chat.packet;

import io.netty.channel.Channel;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

@UtilityClass
public class PlayerChannelResolver {

    public Optional<Channel> resolve(Player player) {
        try {
            Object handle = invoke(player, "getHandle").orElse(null);
            Object gameConnection = field(handle, "connection").orElse(null);
            Object networkConnection = field(gameConnection, "connection").orElse(null);
            Object channel = field(networkConnection, "channel").orElse(null);

            return channel instanceof Channel nettyChannel ? Optional.of(nettyChannel) : Optional.empty();
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    static Optional<Object> invoke(Object target, String methodName) {
        if (target == null) return Optional.empty();

        try {
            Method method = target.getClass().getMethod(methodName);
            method.setAccessible(true);
            return Optional.ofNullable(method.invoke(target));
        } catch (ReflectiveOperationException exception) {
            return Optional.empty();
        }
    }

    static Optional<Object> field(Object target, String fieldName) {
        if (target == null) return Optional.empty();

        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                return Optional.ofNullable(field.get(target));
            } catch (NoSuchFieldException exception) {
                type = type.getSuperclass();
            } catch (ReflectiveOperationException exception) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }
}
