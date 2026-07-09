package gg.moonrise.chat.command;

import gg.moonrise.chat.SpiritChatPlugin;
import gg.moonrise.engine.paper.command.PaperCommand;
import gg.moonrise.moss.spring.SpringComponent;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

@SpringComponent
@RequiredArgsConstructor
public class CommandSpiritChat implements PaperCommand {

    private final SpiritChatPlugin plugin;

    @Command("spiritchat")
    @Permission("spiritchat.admin")
    public void execute(CommandSourceStack source) {
        source.getSender().sendMessage(Component.text("SpiritChat").color(NamedTextColor.AQUA));
    }

    @Command("spiritchat reload")
    @Permission("spiritchat.admin")
    public void reload(CommandSourceStack source) {
        plugin.reload();
        source.getSender().sendMessage(Component.text("SpiritChat reloaded.").color(NamedTextColor.GREEN));
    }
}
