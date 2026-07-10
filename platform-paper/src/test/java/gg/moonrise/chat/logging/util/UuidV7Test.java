package gg.moonrise.chat.logging.util;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UuidV7Test {

    @Test
    void generatesVersionSevenVariantTwoUuids() {
        UUID uuid = UuidV7.generate();

        assertEquals(7, uuid.version());
        assertEquals(2, uuid.variant());
    }

    @Test
    void embedsCurrentUnixTimestampMillis() {
        long before = System.currentTimeMillis();
        UUID uuid = UuidV7.generate();
        long after = System.currentTimeMillis();

        long timestamp = uuid.getMostSignificantBits() >>> 16;

        assertTrue(timestamp >= before);
        assertTrue(timestamp <= after);
    }
}
