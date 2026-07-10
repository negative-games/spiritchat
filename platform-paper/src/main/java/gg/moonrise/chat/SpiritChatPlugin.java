package gg.moonrise.chat;

import gg.moonrise.engine.paper.PaperPlugin;
import gg.moonrise.moss.spring.Loadable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@Slf4j
public class SpiritChatPlugin extends PaperPlugin {

    // Keep Spring scanning away from relocated engine packages that Spirit does not use.
    private static final String[] COMPONENT_PACKAGES = {
            "gg.moonrise.chat.command",
            "gg.moonrise.chat.config",
            "gg.moonrise.chat.controller",
            "gg.moonrise.chat.packet",
            "gg.moonrise.chat.service",
            "gg.moonrise.chat.storage",
            "gg.moonrise.chat.util",
            "gg.moonrise.chat.libs.engine.paper.command",
            "gg.moonrise.chat.libs.engine.paper.platform"
    };

    @Override
    public void onLoad() {
        CONTEXT = new AnnotationConfigApplicationContext();
        CONTEXT.setClassLoader(getClass().getClassLoader());

        loadInitialComponents(CONTEXT);
        for (String componentPackage : COMPONENT_PACKAGES) {
            CONTEXT.scan(componentPackage);
        }

        CONTEXT.refresh();
        CONTEXT.start();

        invokeBeans(
                Loadable.class,
                loadable -> loadable.onLoad(CONTEXT),
                (loadable, exception) -> log.error("Failed to load {}", loadable.getClass().getSimpleName(), exception)
        );
    }
}
