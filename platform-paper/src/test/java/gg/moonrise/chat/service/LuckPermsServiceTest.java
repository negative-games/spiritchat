package gg.moonrise.chat.service;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LuckPermsServiceTest {

    @Test
    void remainsUnavailableWithoutLuckPermsApiOnClasspath() {
        LuckPermsService service = new LuckPermsService();

        assertFalse(service.isAvailable());
        assertTrue(service.loadGroupNames(UUID.randomUUID()).isEmpty());
    }
}
