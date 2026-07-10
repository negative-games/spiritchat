package gg.moonrise.chat.command;

import gg.moonrise.chat.config.ConfigService;
import gg.moonrise.chat.lifecycle.SpiritChatReloadService;
import gg.moonrise.chat.util.PlatformTasks;
import gg.moonrise.engine.paper.command.PaperCommand;
import gg.moonrise.moss.spring.SpringComponent;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

@Slf4j(topic = "SpiritChat")
@SpringComponent
@RequiredArgsConstructor
public class CommandSpiritChatReload implements PaperCommand {

    private final ConfigService configService;
    private final SpiritChatReloadService reloadService;

    @Command("chat|spiritchat reload")
    @Permission("spiritchat.admin")
    public void reload(CommandSourceStack source) {
        PlatformTasks.run(source.getSender(), () -> reloadSync(source));
    }

    private void reloadSync(CommandSourceStack source) {
        try {
            reloadService.reload();
            configService.send(source.getSender(), configService.messages().getGeneral().getReloadSuccess());
        } catch (RuntimeException exception) {
            log.error("Failed to reload SpiritChat.", exception);
            configService.send(source.getSender(), configService.messages().getGeneral().getReloadFailed());
        }
    }

    @Command("chat|spiritchat reload <extra>")
    @Permission("spiritchat.admin")
    public void reloadUsage(CommandSourceStack source, @Argument("extra") @Greedy String extra) {
        PlatformTasks.run(source.getSender(), () -> configService.send(source.getSender(), configService.messages().getGeneral().getReloadUsage()));
    }
}
