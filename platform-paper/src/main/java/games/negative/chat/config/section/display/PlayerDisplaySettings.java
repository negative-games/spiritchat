package games.negative.chat.config.section.display;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;

@Getter
@Configuration
public class PlayerDisplaySettings {

    @Comment({
            "",
            "When true, the player's display name will be overridden by SpiritChat",
            "meaning there may be conflicts with other plugins that also try to set the player's display name.",
            "Set to false to prevent SpiritChat from changing the player's display name.",
            " ",
            "Default: true"
    })
    private boolean overridePlayerDisplayName = true;

}
