package gg.moonrise.chat.logging.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ChatLogText {

    public String normalize(String input, int maxLength) {
        String text = input == null ? "" : input;
        if (text.length() <= maxLength) return text;

        return text.substring(0, maxLength);
    }
}
