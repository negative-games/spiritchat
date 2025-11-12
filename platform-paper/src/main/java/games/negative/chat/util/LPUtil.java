package games.negative.chat.util;

import com.google.common.collect.Lists;
import games.negative.chat.SpiritChatPlugin;
import io.vavr.control.Option;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public final class LPUtil {

    /**
     * Load LuckPerms plugin integration
     * @return LuckPerms instance or null if not found
     */
    public LuckPerms loadLuckPerms() {
        try {
            Class.forName("net.luckperms.api.LuckPermsProvider");
            log.info("Successfully loaded LuckPerms integration!");
            return LuckPermsProvider.get();
        } catch (Exception e) {
            log.warn("LuckPerms plugin not found, features using LuckPerms will not work!", e);
            return null;
        }
    }

    public LinkedList<Group> loadGroups(@NotNull UUID uuid) throws Exception {
        LuckPerms api = SpiritChatPlugin.luckperms().getOrNull();

        User user = api.getUserManager().getUser(uuid);
        if (user == null) throw new Exception("Could not find user with UUID %s".formatted(uuid));

        return user.getInheritedGroups(user.getQueryOptions()).stream()
                .sorted(Comparator.comparingInt(value -> ((Group) value).getWeight().orElse(0)).reversed())
                .collect(Collectors.toCollection(Lists::newLinkedList));
    }

}
