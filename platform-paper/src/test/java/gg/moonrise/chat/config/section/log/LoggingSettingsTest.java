package gg.moonrise.chat.config.section.log;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoggingSettingsTest {

    @Test
    void clampsMalformedMaxMessageLengthToSafeRange() throws Exception {
        LoggingSettings settings = new LoggingSettings();

        setMaxMessageLength(settings, 1);
        assertEquals(64, settings.effectiveMaxMessageLength());

        setMaxMessageLength(settings, 100_000);
        assertEquals(8_192, settings.effectiveMaxMessageLength());
    }

    @Test
    void keepsConfiguredMaxMessageLengthInsideSafeRange() throws Exception {
        LoggingSettings settings = new LoggingSettings();

        setMaxMessageLength(settings, 2_048);

        assertEquals(2_048, settings.effectiveMaxMessageLength());
    }

    private void setMaxMessageLength(LoggingSettings settings, int value) throws Exception {
        Field field = LoggingSettings.class.getDeclaredField("maxMessageLength");
        field.setAccessible(true);
        field.setInt(settings, value);
    }
}
