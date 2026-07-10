package gg.moonrise.chat.service;

import gg.moonrise.chat.controller.ChatController;
import gg.moonrise.moss.spring.SpringComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringComponent
@RequiredArgsConstructor
public class SpiritChatReloadService {

    private final ConfigService configService;
    private final SqlStorageService sqlStorageService;
    private final PlayerMentionOptionsService playerMentionOptionsService;
    private final OnlinePlayerNameService onlinePlayerNameService;
    private final MentionService mentionService;
    private final AntiMessageSigningService antiMessageSigningService;
    private final ChatController chatController;

    public void reload() {
        configService.reload();

        List<RuntimeException> failures = new ArrayList<>();
        for (ReloadStep step : reloadSteps()) {
            try {
                step.reload();
            } catch (RuntimeException exception) {
                failures.add(exception);
                log.error("Failed to reload {}.", step.name(), exception);
            }
        }

        if (!failures.isEmpty()) {
            throw failures.getFirst();
        }
    }

    private List<ReloadStep> reloadSteps() {
        return List.of(
                new ReloadStep("SQL storage", sqlStorageService::reload),
                new ReloadStep("player mention options", playerMentionOptionsService::reload),
                new ReloadStep("online player names", onlinePlayerNameService::reload),
                new ReloadStep("mentions", mentionService::reload),
                new ReloadStep("anti-message-signing", antiMessageSigningService::reload),
                new ReloadStep("chat renderer", chatController::reload)
        );
    }

    private record ReloadStep(String name, Runnable reload) {
    }
}
