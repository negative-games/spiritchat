package gg.moonrise.chat.util;

import gg.moonrise.engine.paper.scheduler.Scheduler;
import lombok.experimental.UtilityClass;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@UtilityClass
public class PlatformTasks {

    public void run(CommandSender sender, Runnable task) {
        if (sender instanceof Player player) {
            run(player, task);
            return;
        }

        runGlobal(task);
    }

    public void run(Player player, Runnable task) {
        try {
            Scheduler.entity(player).execute(() -> {
                if (player.isOnline()) {
                    task.run();
                }
            }, () -> {
            }, 0L);
        } catch (RuntimeException exception) {
        }
    }

    public void runGlobal(Runnable task) {
        try {
            Scheduler.sync().execute(task);
        } catch (RuntimeException exception) {
        }
    }
}
