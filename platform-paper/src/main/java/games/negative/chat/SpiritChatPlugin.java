package games.negative.chat;

import games.negative.alumina.AluminaPlugin;
import games.negative.alumina.command.Command;
import games.negative.alumina.event.Events;
import games.negative.chat.spring.Disableable;
import games.negative.chat.spring.Enableable;
import games.negative.chat.spring.Loadable;
import games.negative.chat.spring.Reloadable;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Slf4j
public class SpiritChatPlugin extends AluminaPlugin {

    private AnnotationConfigApplicationContext context;

    @Override
    public void load() {
        context = new AnnotationConfigApplicationContext();

        context.setClassLoader(getClassLoader());

        context.registerBean(SpiritChatPlugin.class, () -> this);

        context.scan(basePackage());

        context.refresh();

        invokeBeans(Loadable.class, loadable -> loadable.onLoad(context), (loadable, e) -> {
            log.error("Failed to load {}", loadable.getClass().getSimpleName(), e);
        });
    }

    @Override
    public void enable() {
        // Register enableables
        invokeBeans(Enableable.class, Enableable::onEnable, (enableable, e) -> {
            log.error("An error occurred while enabling {}", enableable.getClass().getSimpleName(), e);
        });

        // Initial reload
        reload();

        // Register listeners
        invokeBeans(Listener.class, Events::listen, (listener, e) -> {
            log.error("Failed to register listener {}", listener.getClass().getSimpleName(), e);
        });

        // Register commands
        invokeBeans(Command.class, this::registerCommand, (command, e) -> {
            log.error("Failed to register command {}", command.getClass().getSimpleName(), e);
        });
    }

    @Override
    public void disable() {
        invokeBeans(Disableable.class, Disableable::onDisable, (disableable, e) -> {
            log.error("An error occurred while disabling {}", disableable.getClass().getSimpleName(), e);
        });

        if (context != null) {
            context.close();
            context = null;
        }
    }

    public void reload() {
        invokeBeans(Reloadable.class, Reloadable::onReload, (reloadable, e) -> {
            log.error("An error occurred while reloading {}", reloadable.getClass().getSimpleName(), e);
        });
    }

    /**
     * Invoke all beans of a certain class type with a consumer.
     * @param clazz the class type of the beans to invoke
     * @param consumer the consumer to invoke on each bean
     * @param onFailure the failure consumer to invoke if an exception occurs
     * @param <T> the type of the beans
     */
    private <T> void invokeBeans(@NotNull Class<T> clazz, @NotNull Consumer<T> consumer, @Nullable BiConsumer<T, Exception> onFailure) {
        Collection<T> beans = context.getBeansOfType(clazz).values();
        for (T bean : beans) {
            try {
                consumer.accept(bean);
            } catch (Exception e) {
                if (onFailure == null) return;

                onFailure.accept(bean, e);
            }
        }
    }

    /**
     * Invoke all beans of a certain class type with a consumer.
     * @param clazz the class type of the beans to invoke
     * @param consumer the consumer to invoke on each bean
     * @param <T> the type of the beans
     */
    private <T> void invokeBeans(@NotNull Class<T> clazz, @NotNull Consumer<T> consumer) {
        invokeBeans(clazz, consumer, null);
    }

    protected String basePackage() {
        return this.getClass().getPackageName();
    }
}

