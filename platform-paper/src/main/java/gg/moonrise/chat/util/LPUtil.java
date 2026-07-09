package gg.moonrise.chat.util;

import io.vavr.control.Option;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

@Slf4j
@UtilityClass
public final class LPUtil {

    /**
     * Load LuckPerms plugin integration
     * @return LuckPerms instance if available
     */
    public Option<LuckPerms> loadLuckPerms() {
        try {
            Class.forName("net.luckperms.api.LuckPermsProvider");
            log.info("Successfully loaded LuckPerms integration!");
            return Option.of(LuckPermsProvider.get());
        } catch (Exception e) {
            log.warn("LuckPerms plugin not found, features using LuckPerms will not work!");
            return Option.none();
        }
    }

}
