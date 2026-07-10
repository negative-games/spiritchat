package gg.moonrise.chat.config.section.chat;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MentionSettingsTest {

    @Test
    void fallsBackToDefaultHighlightFormatWhenMalformedConfigSetsNull() throws Exception {
        MentionSettings settings = new MentionSettings();
        setField(settings, "highlightFormat", null);

        assertEquals("<yellow><mention></yellow>", settings.effectiveHighlightFormat());
    }

    @Test
    void treatsNullSoundAsDisabledSound() throws Exception {
        MentionSettings settings = new MentionSettings();
        setField(settings, "sound", null);

        assertEquals("", settings.effectiveSound());
    }

    private void setField(MentionSettings settings, String name, Object value) throws Exception {
        Field field = MentionSettings.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(settings, value);
    }
}
