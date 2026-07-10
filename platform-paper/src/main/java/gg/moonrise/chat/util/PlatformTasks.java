package gg.moonrise.chat.util;

import gg.moonrise.engine.paper.scheduler.Scheduler;
import lombok.experimental.UtilityClass;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@UtilityClass
public class PlatformTasks {

    public boolean run(CommandSender sender, Runnable task) {
        if (sender instanceof Player player) {
            return run(player, task);
        }

        return runGlobal(task);
    }

    public boolean run(Player player, Runnable task) {
        try {
            return Scheduler.entity(player).execute(() -> {
                if (player.isOnline()) {
                    task.run();
                }
            }, () -> {
            }, 0L);
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public boolean runGlobal(Runnable task) {
        try {
            Scheduler.sync().execute(task);
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }
}
