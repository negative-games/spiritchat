package gg.moonrise.chat.mention.command;

import gg.moonrise.chat.config.ConfigService;
import gg.moonrise.chat.config.section.chat.MentionSettings;
import gg.moonrise.chat.mention.service.PlayerMentionOptionsService;
import gg.moonrise.chat.util.PlatformTasks;
import gg.moonrise.engine.paper.command.PaperCommand;
import gg.moonrise.moss.spring.SpringComponent;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.Locale;

@SpringComponent
@RequiredArgsConstructor
public class CommandSpiritChatMentions implements PaperCommand {

    private static final List<String> STATES = List.of("on", "off");

    private final ConfigService configService;
    private final PlayerMentionOptionsService mentionOptionsService;

    @Command("chat|spiritchat mentions")
    public void usage(CommandSourceStack source) {
        sendUsage(source);
    }

    @Command("chat|spiritchat mentions <state>")
    public void mentions(CommandSourceStack source, @Argument(value = "state", suggestions = "mention-states") String state) {
        PlatformTasks.run(source.getSender(), () -> mentionsSync(source, state));
    }

    private void mentionsSync(CommandSourceStack source, String state) {
        if (!(source.getSender() instanceof Player player)) {
            configService.send(source.getSender(), configService.messages().getGeneral().getPlayerOnly());
            return;
        }

        MentionSettings settings = configService.get().getMentionSettings();
        if (!settings.isEnabled()) {
            configService.send(player, configService.messages().getMentions().getFeatureDisabled());
            return;
        }

        if (!settings.isPlayerOptions()) {
            configService.send(player, configService.messages().getMentions().getOptionsDisabled());
            return;
        }

        String normalized = state.toLowerCase(Locale.ROOT);
        if (!STATES.contains(normalized)) {
            configService.send(player, configService.messages().getMentions().getUsage());
            return;
        }

        boolean enabled = normalized.equals("on");
        mentionOptionsService.setPingingEnabled(player, enabled)
                .thenRun(() -> PlatformTasks.run(player, () -> sendSaved(player, enabled)))
                .exceptionally(throwable -> {
                    PlatformTasks.run(player, () -> sendSaveFailed(player));
                    return null;
                });
    }

    private void sendSaveFailed(Player player) {
        if (!player.isOnline()) return;

        configService.send(player, configService.messages().getMentions().getSaveFailed());
    }

    private void sendSaved(Player player, boolean enabled) {
        if (!player.isOnline()) return;

        if (enabled) {
            configService.send(player, configService.messages().getMentions().getEnabled());
        } else {
            configService.send(player, configService.messages().getMentions().getDisabled());
        }
    }

    @Command("chat|spiritchat mentions <state> <extra>")
    public void usage(CommandSourceStack source, @Argument(value = "state", suggestions = "mention-states") String state, @Argument("extra") @Greedy String extra) {
        sendUsage(source);
    }

    @Suggestions("mention-states")
    public Iterable<String> mentionStateSuggestions(CommandContext<CommandSourceStack> context, String input) {
        String normalized = input == null ? "" : input.toLowerCase(Locale.ROOT);
        return STATES.stream()
                .filter(state -> normalized.isBlank() || state.startsWith(normalized))
                .toList();
    }

    private void sendUsage(CommandSourceStack source) {
        PlatformTasks.run(source.getSender(), () -> configService.send(source.getSender(), configService.messages().getMentions().getUsage()));
    }
}
