package gg.moonrise.chat.chat.format;

import gg.moonrise.chat.util.ChatTextSanitizer;
import gg.moonrise.engine.message.util.MiniMessageUtil;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;

import java.util.List;

@UtilityClass
public class ChatMessageFormatter {

    public FormattedChatMessage prepare(String input, List<String> preservedPlaceholders, boolean legacyColorsEnabled) {
        String sanitized = ChatTextSanitizer.sanitizePlainUserMessage(input, preservedPlaceholders);
        if (!legacyColorsEnabled) {
            return new FormattedChatMessage(sanitized, Component.text(sanitized));
        }

        return new FormattedChatMessage(sanitized, MiniMessageUtil.legacyToComponent(sanitized));
    }
}
