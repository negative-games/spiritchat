package games.negative.chat.command;

import games.negative.alumina.command.Command;
import games.negative.alumina.command.CommandContext;
import games.negative.alumina.command.builder.CommandBuilder;
import games.negative.chat.SpiritChatPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

public class CommandSpiritChat extends Command {

    public CommandSpiritChat() {
        super(CommandBuilder.builder()
                .name("spiritchat")
                .permission("spiritchat.admin")
                .smartTabComplete(true)
        );

        injectSubCommand(CommandBuilder.builder().name("reload"), context -> {
            SpiritChatPlugin.getInstance().reloadConfigs();

            context.sender().sendMessage(Component.text("SpiritChat reloaded.").color(NamedTextColor.GREEN));
        });
    }

    @Override
    public void execute(@NotNull CommandContext context) {

    }
}
