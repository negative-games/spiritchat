package gg.moonrise.chat.command;

import gg.moonrise.chat.config.ConfigService;
import gg.moonrise.chat.util.PlatformTasks;
import gg.moonrise.engine.paper.command.PaperCommand;
import gg.moonrise.moss.spring.SpringComponent;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;

@SpringComponent
@RequiredArgsConstructor
public class CommandSpiritChat implements PaperCommand {

    private final ConfigService configService;

    @Command("chat|spiritchat")
    public void execute(CommandSourceStack source) {
        sendHelp(source.getSender());
    }

    @Command("chat|spiritchat help")
    public void help(CommandSourceStack source) {
        sendHelp(source.getSender());
    }

    @Command("chat|spiritchat <extra>")
    public void unknown(CommandSourceStack source, @Argument("extra") @Greedy String extra) {
        sendHelp(source.getSender());
    }

    private void sendHelp(CommandSender sender) {
        PlatformTasks.run(sender, () -> {
            if (sender.hasPermission("spiritchat.admin")) {
                configService.send(sender, configService.messages().getGeneral().getAdminHelp());
                return;
            }

            configService.send(sender, configService.messages().getGeneral().getHelp());
        });
    }
}
