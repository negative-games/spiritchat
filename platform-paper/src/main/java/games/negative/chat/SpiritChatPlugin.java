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
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class SpiritChatPlugin extends AluminaPlugin {

    private AnnotationConfigApplicationContext context;

    private final List<Disableable> disableables = new ArrayList<>();
    private final List<Reloadable> reloadables  = new ArrayList<>();

    @Override
    public void load() {
        context = new AnnotationConfigApplicationContext();

        context.setClassLoader(getClassLoader());

        context.registerBean(SpiritChatPlugin.class, () -> this);

        context.scan(basePackage());

        context.refresh();

        invokeLoadables();
    }

    @Override
    public void enable() {
        setupReloadables();
        setupDisableables();

        invokeEnableables();

        reload();

        invokeListeners();
        invokeCommands();
    }

    @Override
    public void disable() {
        invokeDisableables();

        if (context != null) {
            context.close();
            context = null;
        }
    }

    public void reload() {
        invokeReloadables();
    }

    private void invokeLoadables() {
        String[] loadableBeans = context.getBeanNamesForType(Loadable.class);
        for (String beanName : loadableBeans) {
            Loadable loadable = context.getBean(beanName, Loadable.class);
            try {
                loadable.onLoad(context);
            } catch (Exception e) {
                log.error("Failed to load {}", loadable.getClass().getSimpleName(), e);
            }
        }
    }

    private void invokeEnableables() {
        for (Enableable enableable : context.getBeansOfType(Enableable.class).values()) {
            try {
                enableable.onEnable();
            } catch (Exception e) {
                log.error("An error occurred while enabling {}", enableable.getClass().getSimpleName(), e);
            }
        }
    }

    private void setupDisableables() {
        disableables.clear();
        disableables.addAll(context.getBeansOfType(Disableable.class).values());
    }

    private void invokeDisableables() {
        for (Disableable disableable : disableables) {
            try {
                disableable.onDisable();
            } catch (Exception e) {
                log.error("An error occurred while disabling {}",
                        disableable.getClass().getSimpleName(), e);
            }
        }
    }

    private void setupReloadables() {
        reloadables.clear();
        reloadables.addAll(context.getBeansOfType(Reloadable.class).values());
    }

    private void invokeReloadables() {
        for (Reloadable reloadable : reloadables) {
            try {
                reloadable.onReload();
            } catch (Exception e) {
                log.error("An error occurred while reloading {}", reloadable.getClass().getSimpleName(), e);
            }
        }
    }

    private void invokeListeners() {
        Collection<Listener> listeners = context.getBeansOfType(Listener.class).values();
        for (Listener listener : listeners) {
            try {
                Events.listen(listener);
            } catch (Exception e) {
                log.error("Failed to register listener {}", listener.getClass().getName(), e);
            }
        }
    }

    private void invokeCommands() {
        Collection<Command> commands = context.getBeansOfType(Command.class).values();
        for (Command command : commands) {
            try {
                registerCommand(command);
            } catch (Exception e) {
                log.error("Failed to register command {}", command.getClass().getName(), e);
            }
        }
    }

    protected String basePackage() {
        return this.getClass().getPackageName();
    }
}

