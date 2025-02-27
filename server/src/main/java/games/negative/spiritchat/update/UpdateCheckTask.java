package games.negative.spiritchat.update;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import games.negative.alumina.event.Events;
import games.negative.alumina.message.Message;
import games.negative.alumina.util.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import games.negative.spiritchat.SpiritChatPlugin;
import games.negative.spiritchat.permission.Perm;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class UpdateCheckTask extends BukkitRunnable {

    private static final String URL = "https://ver.ericlmao.com/spiritchat";

    private static final Message MESSAGE = new Message("<click:open_url:'https://modrinth.com/project/spiritchat/versions'><yellow><b>UPDATE AVAILABLE!</b></yellow> <gray>A new update for <yellow>SpiritChat <dark_gray>[<green>%latest%</green>]</dark_gray></yellow> is available!</gray></click>");

    private final String current;

    public UpdateCheckTask(@NotNull String current) {
        this.current = current;

        Events.listen(UpdateAvailableEvent.class, event -> {
            MESSAGE.create()
                    .replace("%latest%", event.getLatest())
                    .send(Bukkit.getConsoleSender());

            Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.hasPermission(Perm.UPDATE_NOTIFICATIONS))
                    .forEach(player -> MESSAGE.create()
                            .replace("%latest%", event.getLatest())
                            .send(player));
        });
    }

    @Override
    public void run() {
        // Don't check for updates if the config is set to false
        if (!SpiritChatPlugin.config().checkForUpdates()) return;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) return;

            String responseBody = response.body();

            // Parse JSON using Gson
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

            if (!jsonObject.has("version")) return;

            String version = jsonObject.get("version").getAsString();

            if (current.equals(version)) return;

            Tasks.run(() -> {
                UpdateAvailableEvent event = new UpdateAvailableEvent(current, version);
                event.callEvent();
            });

        } catch (Exception ignored) {
        }
    }
}
