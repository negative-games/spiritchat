package gg.moonrise.chat.config.section.chat;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;

@Getter
@Configuration
public class AntiMessageSigningSettings {

    @Comment({
            "Helps formatted chat work with modern signed-chat clients.",
            "Most servers should leave this enabled.",
            " ",
            "Default: true"
    })
    private boolean enabled = true;

    @Comment({
            "",
            "Sends formatted player chat as system chat.",
            "This prevents modified SpiritChat messages from being treated as reportable signed chat.",
            " ",
            "Default: true"
    })
    private boolean rewritePlayerChat = true;

    @Comment({
            "",
            "Advertises report prevention in the server list status response.",
            "Some clients use this to avoid reportability warnings before joining.",
            " ",
            "Default: true"
    })
    private boolean sendPreventsChatReportsToClient = true;

    @Comment({
            "",
            "Claims secure chat is enforced during login.",
            "Leave this disabled unless you have tested the client behavior you want.",
            "This does not replace enforce-secure-profile=false in server.properties for unsigned clients.",
            " ",
            "Default: false"
    })
    private boolean claimSecureChatEnforced = false;

    @Comment({
            "",
            "Only rewrite player chat for Bedrock-style UUIDs.",
            "Leave false for normal Java/Paper servers.",
            "This only affects rewrite-player-chat.",
            " ",
            "Default: false"
    })
    private boolean bedrockOnly = false;

    public boolean hasAnyActiveFeature() {
        return rewritePlayerChat || sendPreventsChatReportsToClient || claimSecureChatEnforced;
    }
}
