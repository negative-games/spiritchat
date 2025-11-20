package games.negative.chat.util;

import games.negative.alumina.message.Message;
import games.negative.chat.SpiritChatPlugin;
import games.negative.chat.config.section.chat.ChatItemSettings;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Pattern;

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
