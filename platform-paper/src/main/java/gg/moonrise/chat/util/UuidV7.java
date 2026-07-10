package gg.moonrise.chat.util;

import lombok.experimental.UtilityClass;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class UuidV7 {

    public UUID generate() {
        long timestamp = System.currentTimeMillis();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        long most = (timestamp << 16) | 0x7000L | random.nextLong(0x1000L);
        long least = (random.nextLong() & 0x3FFF_FFFF_FFFF_FFFFL) | 0x8000_0000_0000_0000L;
        return new UUID(most, least);
    }
}
