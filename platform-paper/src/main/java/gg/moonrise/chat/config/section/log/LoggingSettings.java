package gg.moonrise.chat.config.section.log;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;

@Getter
@Configuration
public class LoggingSettings {

    private static final int DEFAULT_MAX_MESSAGE_LENGTH = 1_024;
    private static final int MIN_MAX_MESSAGE_LENGTH = 64;
    private static final int MAX_MAX_MESSAGE_LENGTH = 8_192;

    @Comment({
            "",
            "Stores player chat messages in the configured database.",
            "Enable this if staff need to review chat history with /chat logs.",
            " ",
            "Changes here apply after /chat reload.",
            " ",
            "Default: false"
    })
    private boolean enabled = false;

    @Comment({
            "",
            "Maximum plain-text message length stored for each chat log row.",
            "Longer messages are truncated before they are written to storage.",
            "Allowed range: 64-8192.",
            " ",
            "Default: 1024"
    })
    private int maxMessageLength = DEFAULT_MAX_MESSAGE_LENGTH;

    public int effectiveMaxMessageLength() {
        return Math.max(MIN_MAX_MESSAGE_LENGTH, Math.min(MAX_MAX_MESSAGE_LENGTH, maxMessageLength));
    }
}
