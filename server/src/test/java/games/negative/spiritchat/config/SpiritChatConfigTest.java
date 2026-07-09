package games.negative.spiritchat.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpiritChatConfigTest {

    @Test
    void defaultConfigKeepsStableBetaBehaviorEnabled() {
        SpiritChatConfig config = new SpiritChatConfig();

        assertTrue(config.checkForUpdates());
        assertTrue(config.bStats());
        assertTrue(config.format().useItemDisplay());
        assertTrue(config.format().useStaticFormat());
    }

    @Test
    void defaultStaticFormatUsesSupportedPlaceholders() {
        SpiritChatConfig config = new SpiritChatConfig();

        assertEquals(
                "<gray>%username%</gray> <dark_gray>></dark_gray> <white>%message%</white>",
                config.format().globalFormat().orElseThrow()
        );
    }

    @Test
    void defaultGroupFormatsIncludeDefaultAndAdmin() {
        SpiritChatConfig config = new SpiritChatConfig();

        assertTrue(config.format().groupFormat("default").isPresent());
        assertTrue(config.format().groupFormat("admin").isPresent());
        assertFalse(config.format().groupFormat("missing").isPresent());
    }
}
