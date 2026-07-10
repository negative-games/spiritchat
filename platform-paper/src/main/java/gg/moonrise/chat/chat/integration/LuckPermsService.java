package gg.moonrise.chat.chat.integration;

import gg.moonrise.moss.spring.Enableable;
import gg.moonrise.moss.spring.SpringComponent;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;

@SpringComponent
@Slf4j
public class LuckPermsService implements Enableable {

    private volatile Option<LuckPermsAccess> luckPerms = Option.none();
    private volatile boolean attemptedLoad = false;

    public boolean isAvailable() {
        return luckPermsAccess().isDefined();
    }

    private Option<LuckPermsAccess> luckPermsAccess() {
        if (!attemptedLoad) {
            synchronized (this) {
                if (!attemptedLoad) {
                    loadLuckPerms();
                }
            }
        }

        return luckPerms;
    }

    public List<String> loadGroupNames(@NotNull UUID uuid) {
        LuckPermsAccess access = luckPermsAccess().getOrNull();
        if (access == null) return List.of();

        try {
            return access.loadGroupNames(uuid);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            log.debug("Could not load LuckPerms groups for UUID {}.", uuid, exception);
            return List.of();
        }
    }

    @Override
    public void onEnable() {
        synchronized (this) {
            loadLuckPerms();
        }
    }

    private void loadLuckPerms() {
        attemptedLoad = true;
        try {
            luckPerms = Option.of(LuckPermsAccess.load());
            log.info("Successfully loaded LuckPerms integration.");
        } catch (ReflectiveOperationException | RuntimeException exception) {
            luckPerms = Option.none();
            log.debug("LuckPerms integration is unavailable.", exception);
        }
    }

    private record LuckPermsAccess(
            Object api,
            Method getUserManager,
            Method getUser,
            Method getQueryOptions,
            Method getInheritedGroups,
            Method getName,
            Method getWeight
    ) {

        private static LuckPermsAccess load() throws ReflectiveOperationException {
            Class<?> provider = Class.forName("net.luckperms.api.LuckPermsProvider");
            Object api = provider.getMethod("get").invoke(null);
            Class<?> luckPermsType = Class.forName("net.luckperms.api.LuckPerms");
            Class<?> userManagerType = Class.forName("net.luckperms.api.model.user.UserManager");
            Method getUser = userManagerType.getMethod("getUser", UUID.class);
            Class<?> userType = Class.forName("net.luckperms.api.model.user.User");
            Method getQueryOptions = userType.getMethod("getQueryOptions");
            Method getInheritedGroups = userType.getMethod("getInheritedGroups", getQueryOptions.getReturnType());
            Class<?> groupType = Class.forName("net.luckperms.api.model.group.Group");

            return new LuckPermsAccess(
                    api,
                    luckPermsType.getMethod("getUserManager"),
                    getUser,
                    getQueryOptions,
                    getInheritedGroups,
                    groupType.getMethod("getName"),
                    groupType.getMethod("getWeight")
            );
        }

        private List<String> loadGroupNames(UUID playerId) throws ReflectiveOperationException {
            Object userManager = getUserManager.invoke(api);
            Object user = getUser.invoke(userManager, playerId);
            if (user == null) return List.of();

            Object queryOptions = getQueryOptions.invoke(user);
            Object inheritedGroups = getInheritedGroups.invoke(user, queryOptions);
            if (!(inheritedGroups instanceof Iterable<?> groups)) {
                return List.of();
            }

            List<GroupView> names = new ArrayList<>();
            for (Object group : groups) {
                names.add(new GroupView(
                        (String) getName.invoke(group),
                        weight(group)
                ));
            }

            names.sort((first, second) -> Integer.compare(second.weight(), first.weight()));
            return names.stream()
                    .map(GroupView::name)
                    .toList();
        }

        private int weight(Object group) throws ReflectiveOperationException {
            Object weight = getWeight.invoke(group);
            if (weight instanceof OptionalInt optionalInt) {
                return optionalInt.orElse(0);
            }
            if (weight instanceof java.util.Optional<?> optional) {
                return optional.map(Number.class::cast)
                        .map(Number::intValue)
                        .orElse(0);
            }
            if (weight instanceof Number number) {
                return number.intValue();
            }
            return 0;
        }
    }

    private record GroupView(String name, int weight) {
    }
}
