package gg.moonrise.chat.config.section.chat;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;

@Getter
@Configuration
public class AntiMessageSigningSettings {

    @Comment({
            "Whether outgoing player chat packets should be rewritten as system chat packets.",
            "This makes formatted chat unreportable and avoids clients rejecting modified signed messages.",
            "Supported on Paper 1.21.8+ while the server's chat packet structure remains compatible.",
            " ",
            "Default: true"
    })
    private boolean enabled = true;

    @Comment({
            "",
            "Whether the login packet should tell clients that secure chat is enforced.",
            "Leave this disabled unless you specifically want vanilla clients to require signed chat.",
            "Enabling it can cause clients without an accepted profile key to block chat.",
            " ",
            "Default: false"
    })
    private boolean claimSecureChatEnforced = false;

    @Comment({
            "",
            "Only enable packet rewriting for Bedrock players.",
            "Leave false unless you specifically want Java players to keep reportable signed chat.",
            " ",
            "Default: false"
    })
    private boolean bedrockOnly = false;
}
