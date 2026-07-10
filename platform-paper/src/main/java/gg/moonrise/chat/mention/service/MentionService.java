package gg.moonrise.chat.mention.service;

import gg.moonrise.chat.config.section.chat.MentionSettings;
import gg.moonrise.chat.config.ConfigService;
import gg.moonrise.chat.util.PlatformTasks;
import gg.moonrise.engine.message.util.MiniMessageUtil;
import gg.moonrise.engine.state.Reloadable;
import gg.moonrise.moss.spring.SpringComponent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SpringComponent
@RequiredArgsConstructor
@Slf4j
public class MentionService implements Listener, Reloadable {

    private static final String NAME_BOUNDARY = "[A-Za-z0-9_]";
    private static final Pattern MENTION_CANDIDATE = Pattern.compile("(?i)(?<!" + NAME_BOUNDARY + ")@([A-Za-z0-9_]{1,16})(?!" + NAME_BOUNDARY + ")");

    private final ConfigService configService;
    private final PlayerMentionOptionsService optionsService;
    private final Map<UUID, MentionTarget> targetsById = new ConcurrentHashMap<>();
    private final Map<String, MentionTarget> targetsByName = new ConcurrentHashMap<>();
    private volatile MentionSound mentionSound;

    @PostConstruct
    public void init() {
        mentionSound = sound(configService.get().getMentionSettings());
    }

    @Override
    public void reload() {
        mentionSound = sound(configService.get().getMentionSettings());
        targetsById.clear();
        targetsByName.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            track(player);
            optionsService.preload(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        track(player);
        optionsService.preload(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        untrack(player.getUniqueId());
        optionsService.invalidate(player);
    }

    public Component applyHighlights(Player source, String input, Component component) {
        MentionSettings settings = configService.get().getMentionSettings();
        if (!settings.isEnabled() || !settings.isHighlight()) return component;

        Component highlighted = component;
        for (MentionTarget target : mentionedTargets(input)) {
            highlighted = highlighted.replaceText(TextReplacementConfig.builder()
                    .match(target.pattern())
                    .replacement((match, builder) -> highlightComponent(source, settings, target.playerName(), match.group()))
                    .build());
        }

        return highlighted;
    }

    public void notifyMentionedPlayers(Player sender, String input, Set<Audience> viewers) {
        MentionSettings settings = configService.get().getMentionSettings();
        if (!settings.isEnabled() || !settings.isPinging()) return;
        if (input == null || input.isBlank()) return;

        Set<UUID> mentionedPlayerIds = mentionedTargets(input).stream()
                .map(MentionTarget::playerId)
                .filter(playerId -> canSeeChat(viewers, playerId))
                .collect(Collectors.toUnmodifiableSet());

        if (mentionedPlayerIds.isEmpty()) return;

        String senderName = sender.getName();
        PlatformTasks.runGlobal(() -> {
            for (UUID playerId : mentionedPlayerIds) {
                pingIfOnline(senderName, playerId);
            }
        });
    }

    private void pingIfOnline(String senderName, UUID recipientId) {
        Player recipient = Bukkit.getPlayer(recipientId);
        if (recipient == null || !recipient.isOnline()) return;

        PlatformTasks.run(recipient, () -> {
            if (!optionsService.isPingingEnabled(recipient)) return;

            ping(senderName, recipient);
        });
    }

    private void ping(String senderName, Player recipient) {
        recipient.sendActionBar(configService.component(
                recipient,
                configService.messages().getMentions().getActionBar(),
                Placeholder.unparsed("player", senderName)
        ));

        MentionSound sound = mentionSound;
        if (sound == null) return;

        sound.play(recipient);
    }

    private boolean canSeeChat(Set<Audience> viewers, UUID playerId) {
        if (viewers == null || viewers.isEmpty()) return true;

        boolean hasPlayerViewers = false;

        for (Audience viewer : viewers) {
            if (!(viewer instanceof Player player)) continue;

            hasPlayerViewers = true;
            if (player.getUniqueId().equals(playerId)) {
                return true;
            }
        }

        return !hasPlayerViewers;
    }

    private Component highlightComponent(Player source, MentionSettings settings, String playerName, String mention) {
        return MiniMessageUtil.fromText(
                source,
                settings.effectiveHighlightFormat(),
                Placeholder.unparsed("mention", mention),
                Placeholder.unparsed("player", playerName)
        );
    }

    private Pattern mentionPattern(String playerName) {
        return Pattern.compile("(?i)(?<!" + NAME_BOUNDARY + ")@" + Pattern.quote(playerName) + "(?!" + NAME_BOUNDARY + ")");
    }

    private void track(Player player) {
        untrack(player.getUniqueId());

        MentionTarget target = new MentionTarget(
                player.getUniqueId(),
                player.getName(),
                mentionPattern(player.getName())
        );
        targetsById.put(player.getUniqueId(), target);
        targetsByName.put(normalizeName(player.getName()), target);
    }

    private void untrack(UUID playerId) {
        MentionTarget target = targetsById.remove(playerId);
        if (target != null) {
            targetsByName.remove(normalizeName(target.playerName()), target);
        }
    }

    private Set<MentionTarget> mentionedTargets(String input) {
        Set<MentionTarget> targets = new LinkedHashSet<>();
        java.util.regex.Matcher matcher = MENTION_CANDIDATE.matcher(input);
        while (matcher.find()) {
            MentionTarget target = targetsByName.get(normalizeName(matcher.group(1)));
            if (target != null) {
                targets.add(target);
            }
        }
        return targets;
    }

    private String normalizeName(String playerName) {
        return playerName.toLowerCase(Locale.ROOT);
    }

    private MentionSound sound(MentionSettings settings) {
        String soundKey = settings.effectiveSound();
        if (soundKey.isBlank()) return null;

        return new MentionSound(
                soundKey,
                boundedSoundValue(settings.getSoundVolume(), 1.0D, 0.0D, 10.0D),
                boundedSoundValue(settings.getSoundPitch(), 1.0D, 0.5D, 2.0D)
        );
    }

    private float boundedSoundValue(double value, double fallback, double min, double max) {
        if (!Double.isFinite(value)) {
            return (float) fallback;
        }

        return (float) Math.clamp(value, min, max);
    }

    private record MentionTarget(UUID playerId, String playerName, Pattern pattern) {
    }

    private record MentionSound(String key, float volume, float pitch) {

        private void play(Player player) {
            player.playSound(player.getLocation(), key, SoundCategory.PLAYERS, volume, pitch);
        }
    }
}
