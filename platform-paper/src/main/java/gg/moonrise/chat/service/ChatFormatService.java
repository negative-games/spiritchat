package gg.moonrise.chat.service;

import gg.moonrise.engine.message.util.MiniMessageUtil;
import gg.moonrise.moss.spring.SpringComponent;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

@SpringComponent
@RequiredArgsConstructor
public class ChatFormatService {

    private final ConfigService configService;
    private final MentionService mentionService;
    private final ChatItemService chatItemService;

    public Component applyFormat(Player player, Component sourceDisplayName, String format, Component original) {
        String input = MiniMessageUtil.componentToPlainText(original);
        Component highlightedMessage = mentionService.applyHighlights(player, input, original);
        Component formattedMessage = chatItemService.apply(player, input, highlightedMessage);

        return MiniMessageUtil.fromText(
                player,
                format,
                Placeholder.component("player", sourceDisplayName),
                Placeholder.component("message", formattedMessage)
        );
    }

    public FormattedChatMessage prepareMessage(Player player, Component message) {
        String input = MiniMessageUtil.componentToPlainText(message);
        return ChatMessageFormatter.prepare(
                input,
                configService.get().getChatItemSettings().effectivePlaceholders(),
                player.hasPermission("spiritchat.chat-colors")
        );
    }
}
