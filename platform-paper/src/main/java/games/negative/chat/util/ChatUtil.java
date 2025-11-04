package games.negative.chat.util;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

@UtilityClass
public final class ChatUtil {

    public MiniMessage MINIMESSAGE = MiniMessage.miniMessage();

    public PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();

    public LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.AMPERSAND_CHAR)
            .useUnusualXRepeatedCharacterHexFormat()
            .hexCharacter(LegacyComponentSerializer.HEX_CHAR)
            .hexColors()
            .build();

    public String formatMessage(Player player, Component message) {
        String text;
        if (!player.hasPermission("spiritchat.chat-colors")) {
            text = MINIMESSAGE.escapeTags(PLAIN_SERIALIZER.serialize(message));
        } else {
            Component component = LEGACY_SERIALIZER.deserialize(PLAIN_SERIALIZER.serialize(message));
            text = MINIMESSAGE.serialize(component);
        }

        return text;
    }

    public void setMiniMessage(MiniMessage miniMessage) {
        MINIMESSAGE = miniMessage;
    }

    public void setPlainSerializer(PlainTextComponentSerializer plainTextComponentSerializer) {
        PLAIN_SERIALIZER = plainTextComponentSerializer;
    }

    public void setLegacySerializer(LegacyComponentSerializer legacyComponentSerializer) {
        LEGACY_SERIALIZER = legacyComponentSerializer;
    }
}
