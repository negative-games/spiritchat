package games.negative.chat.service;

import com.google.common.collect.Lists;
import games.negative.chat.spring.Enableable;
import games.negative.chat.spring.SpringComponent;
import games.negative.chat.util.LPUtil;
import io.vavr.control.Option;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.UUID;
import java.util.stream.Collectors;

@SpringComponent
public class LuckPermsService implements Enableable{

    private Option<LuckPerms> luckPerms;

    public Option<LuckPerms> luckPerms() {
        return luckPerms;
    }

    public LinkedList<Group> loadGroups(@NotNull UUID uuid) throws Exception {
        LuckPerms api = luckPerms().getOrNull();

        User user = api.getUserManager().getUser(uuid);
        if (user == null) throw new Exception("Could not find user with UUID %s".formatted(uuid));

        return user.getInheritedGroups(user.getQueryOptions()).stream()
                .sorted(Comparator.comparingInt(value -> ((Group) value).getWeight().orElse(0)).reversed())
                .collect(Collectors.toCollection(Lists::newLinkedList));
    }

    @Override
    public void onEnable() {
        luckPerms = Option.of(LPUtil.loadLuckPerms());
    }
}
