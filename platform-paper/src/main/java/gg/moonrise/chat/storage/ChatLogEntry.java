package gg.moonrise.chat.storage;

import java.util.UUID;

public record ChatLogEntry(
        UUID id,
        UUID senderId,
        String senderName,
        String message,
        String world,
        long serverTime
) {
}
