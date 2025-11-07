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

    public Component applyFormat(Player player, Message format, Component original) {
        String input = formatMessage(player, original);

        Message.Builder builder = format.create(MINIMESSAGE);
        builder.replace(Pattern.quote("{player}"), player.getName());
        builder.replace(Pattern.quote("{message}"), input);

        formatChatItemMessage(player, input, builder);

        return builder.asComponent(player);
    }

    public void formatChatItemMessage(Player player, String input, Message.Builder builder) {
        ChatItemSettings settings = SpiritChatPlugin.config().getChatItemSettings();
        if (!settings.isEnabled() || !settings.containsChatItemSyntax(input)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (settings.isItemTypeBlocked(item.getType())) return;

        for (String placeholder : settings.getPlaceholders()) {
            builder.replace(Pattern.quote(placeholder), "<white>%spiritchat-item%</white>");
        }

        Component name = item.effectiveName().hoverEvent(item.asHoverEvent());
        builder.replace("%spiritchat-item%", name);
    }

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
