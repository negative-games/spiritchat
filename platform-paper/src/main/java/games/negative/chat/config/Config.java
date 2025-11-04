package games.negative.chat.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;

@Getter
@Configuration
public class Config {

    @Comment({
            "Whether or not to check for updates."
    })
    private boolean checkForUpdates = true;

}
