package games.negative.chat.command;

import games.negative.alumina.command.Command;
import games.negative.alumina.command.CommandContext;
import games.negative.alumina.command.builder.CommandBuilder;
import games.negative.chat.SpiritChatPlugin;
import games.negative.chat.spring.SpringComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

@SpringComponent
public class CommandSpiritChat extends Command {

    private final SpiritChatPlugin plugin;
    public CommandSpiritChat(SpiritChatPlugin plugin) {
        super(CommandBuilder.builder()
                .name("spiritchat")
                .permission("spiritchat.admin")
                .smartTabComplete(true)
        );
        this.plugin = plugin;

        injectSubCommand(CommandBuilder.builder().name("reload"), context -> {
            plugin.reload();

            context.sender().sendMessage(Component.text("SpiritChat reloaded.").color(NamedTextColor.GREEN));
        });
    }

    @Override
    public void execute(@NotNull CommandContext context) {

    }
}
