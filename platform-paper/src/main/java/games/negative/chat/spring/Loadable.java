package games.negative.chat.spring;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.support.GenericApplicationContext;

public interface Loadable {

    /**
     * Called when the application context is being loaded.
     * @param context The application context.
     */
    void onLoad(@NotNull GenericApplicationContext context);

}
