package gg.moonrise.chat.chat.integration;

import gg.moonrise.moss.spring.Enableable;
import gg.moonrise.moss.spring.SpringComponent;
import lombok.extern.slf4j.Slf4j;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@SpringComponent
@Slf4j(topic = "SpiritChat")
public class LuckPermsService implements Enableable {

    private volatile LuckPermsAccess luckPerms;
    private volatile boolean attemptedLoad = false;

    public boolean isAvailable() {
        return luckPerms() != null;
    }

    private LuckPermsAccess luckPerms() {
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
        LuckPermsAccess access = luckPerms();
        if (access == null) return List.of();

        try {
            return access.loadGroupNames(uuid);
        } catch (RuntimeException exception) {
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
            luckPerms = LuckPermsApiAccess.load();
            log.info("Successfully loaded LuckPerms integration.");
        } catch (IllegalStateException | NoClassDefFoundError exception) {
            luckPerms = null;
            log.debug("LuckPerms integration is unavailable.", exception);
        }
    }

    private interface LuckPermsAccess {

        List<String> loadGroupNames(UUID playerId);
    }

    private record LuckPermsApiAccess(LuckPerms api) implements LuckPermsAccess {

        private static LuckPermsApiAccess load() {
            return new LuckPermsApiAccess(LuckPermsProvider.get());
        }

        @Override
        public List<String> loadGroupNames(UUID playerId) {
            User user = api.getUserManager().getUser(playerId);
            if (user == null) return List.of();

            return user.getInheritedGroups(user.getQueryOptions()).stream()
                    .sorted(Comparator.comparingInt((Group group) -> group.getWeight().orElse(0)).reversed())
                    .map(Group::getName)
                    .toList();
        }
    }
}
