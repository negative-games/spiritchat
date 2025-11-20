package games.negative.chat.util;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

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
