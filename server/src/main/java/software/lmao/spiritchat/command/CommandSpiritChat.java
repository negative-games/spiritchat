package software.lmao.spiritchat.command;

import games.negative.alumina.command.Command;
import games.negative.alumina.command.CommandContext;
import games.negative.alumina.command.builder.CommandBuilder;
import org.jetbrains.annotations.NotNull;
import software.lmao.spiritchat.SpiritChatPlugin;
import software.lmao.spiritchat.permission.Perm;

public class CommandSpiritChat extends Command {

    public CommandSpiritChat() {
        super(CommandBuilder.builder().name("spiritchat")
                .description("The administrative command for SpiritChat.")
                .permission(Perm.ADMIN)
                .smartTabComplete(true)
        );

        injectSubCommand(CommandBuilder.builder().name("reload"), context -> {
            SpiritChatPlugin.instance().reload();

            SpiritChatPlugin.messages().getReloaded().create().send(context.sender());
        });
    }

    @Override
    public void execute(@NotNull CommandContext context) {
        SpiritChatPlugin.messages().getAdminCommandHelp().create().send(context.sender());
    }
}
