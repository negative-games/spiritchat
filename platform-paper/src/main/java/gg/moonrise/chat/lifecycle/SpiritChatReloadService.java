package gg.moonrise.chat.lifecycle;

import gg.moonrise.chat.chat.listener.ChatListener;
import gg.moonrise.chat.chat.item.ChatItemService;
import gg.moonrise.chat.config.ConfigService;
import gg.moonrise.chat.mention.service.MentionService;
import gg.moonrise.chat.signing.service.AntiMessageSigningService;
import gg.moonrise.chat.storage.SqlStorageService;
import gg.moonrise.moss.spring.SpringComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j(topic = "SpiritChat")
@SpringComponent
@RequiredArgsConstructor
public class SpiritChatReloadService {

    private final ConfigService configService;
    private final SqlStorageService sqlStorageService;
    private final ChatItemService chatItemService;
    private final MentionService mentionService;
    private final AntiMessageSigningService antiMessageSigningService;
    private final ChatListener chatListener;

    public void reload() {
        long started = System.nanoTime();
        log.info("Reloading SpiritChat from disk...");

        configService.reload();

        List<RuntimeException> failures = new ArrayList<>();
        for (ReloadStep step : reloadSteps()) {
            long stepStarted = System.nanoTime();
            try {
                step.reload().run();
                log.info("Reloaded {} in {}ms.", step.name(), elapsedMillis(stepStarted));
            } catch (RuntimeException exception) {
                failures.add(exception);
                log.error("Failed to reload {}.", step.name(), exception);
            }
        }

        if (!failures.isEmpty()) {
            throw failures.getFirst();
        }

        log.info("Reload complete in {}ms.", elapsedMillis(started));
    }

    private List<ReloadStep> reloadSteps() {
        return List.of(
                new ReloadStep("SQL storage", sqlStorageService::reload),
                new ReloadStep("chat item", chatItemService::reload),
                new ReloadStep("mentions", mentionService::reload),
                new ReloadStep("anti-message-signing", antiMessageSigningService::reload),
                new ReloadStep("chat renderer", () -> {
                    chatListener.reload();
                    log.info("Chat renderer: {}.", chatListener.rendererDescription());
                })
        );
    }

    private record ReloadStep(String name, Runnable reload) {
    }

    private long elapsedMillis(long started) {
        return (System.nanoTime() - started) / 1_000_000L;
    }
}
