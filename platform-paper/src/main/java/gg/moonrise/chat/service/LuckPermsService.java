package gg.moonrise.chat.service;

import gg.moonrise.chat.util.LPUtil;
import gg.moonrise.moss.spring.Enableable;
import gg.moonrise.moss.spring.SpringComponent;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.UUID;
import java.util.stream.Collectors;

@SpringComponent
@Slf4j
public class LuckPermsService implements Enableable {

    private Option<LuckPerms> luckPerms = Option.none();
    private boolean attemptedLoad = false;

    public Option<LuckPerms> luckPerms() {
        if (!attemptedLoad) {
            loadLuckPerms();
        }

        return luckPerms;
    }

    public LinkedList<Group> loadGroups(@NotNull UUID uuid) {
        LuckPerms api = luckPerms().getOrNull();
        if (api == null) return new LinkedList<>();

        User user = api.getUserManager().getUser(uuid);
        if (user == null) {
            log.debug("Could not find LuckPerms user with UUID {}", uuid);
            return new LinkedList<>();
        }

        return user.getInheritedGroups(user.getQueryOptions()).stream()
                .sorted(Comparator.comparingInt((Group group) -> group.getWeight().orElse(0)).reversed())
                .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public void onEnable() {
        loadLuckPerms();
    }

    private void loadLuckPerms() {
        attemptedLoad = true;
        luckPerms = LPUtil.loadLuckPerms();
    }
}
