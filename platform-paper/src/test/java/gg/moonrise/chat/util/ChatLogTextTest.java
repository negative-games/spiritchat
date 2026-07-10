package gg.moonrise.chat.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatLogTextTest {

    @Test
    void normalizesNullMessagesToEmptyText() {
        assertEquals("", ChatLogText.normalize(null, 10));
    }

    @Test
    void keepsMessagesWithinLimit() {
        assertEquals("hello", ChatLogText.normalize("hello", 10));
    }

    @Test
    void truncatesMessagesOverLimit() {
        assertEquals("hello", ChatLogText.normalize("hello world", 5));
    }
}
