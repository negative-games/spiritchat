package games.negative.spiritchat.loader;

import com.google.common.base.Preconditions;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

@Slf4j
public class SpiritChatPluginLoader implements PluginLoader {

    @Override
    public void classloader(PluginClasspathBuilder builder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolver.addRepository(new RemoteRepository.Builder("central", "default", getDefaultMavenCentralMirror()).build());

        try (InputStream input = getClass().getResourceAsStream("/internal/dependencies.yml")) {
            Preconditions.checkNotNull(input, "Could not find internal/dependencies.yml");

            try (Reader reader = new InputStreamReader(input)) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(reader);

                ConfigurationSection repos = config.getConfigurationSection("repositories");
                Preconditions.checkNotNull(repos, "repositories section is missing in internal/dependencies.yml");

                List<YamlRepository> repositories = repos.getKeys(false).stream()
                        .filter(s -> repos.getConfigurationSection(s) != null)
                        .map(s -> YamlRepository.fromConfigurationSection(repos.getConfigurationSection(s)))
                        .toList();

                for (YamlRepository repository : repositories) {
                    resolver.addRepository(new RemoteRepository.Builder(repository.id(), "default", repository.url()).build());
                }

                for (String dependencies : config.getStringList("dependencies")) {
                    resolver.addDependency(new Dependency(new DefaultArtifact(dependencies), null));
                }
            }
        } catch (IOException e) {
            log.error("Failed to load internal/dependencies.yml", e);
        }

        builder.addLibrary(resolver);
    }

    public record YamlRepository(@NotNull String id, @NotNull String url) {

        public static YamlRepository fromConfigurationSection(ConfigurationSection section) {
            Preconditions.checkNotNull(section, "Repository section is missing in internal/dependencies.yml");

            String id = section.getString("id");
            Preconditions.checkNotNull(id, "Repository id is missing in section'" + section.getName() + "' in internal/dependencies.yml");

            String url = section.getString("url");
            Preconditions.checkNotNull(url, "Repository url is missing in section '" + section.getName() + "' in internal/dependencies.yml");

            return new YamlRepository(id, url);
        }
    }

    /**
     * Backported from <a href="https://github.com/PaperMC/Paper/commit/62b7f86dae659deb2fc450285452d7c1439f92dc">...</a>
     * @return the default Maven Central mirror URL
     */
    private static String getDefaultMavenCentralMirror() {
        String central = System.getenv("PAPER_DEFAULT_CENTRAL_REPOSITORY");
        if (central == null) {
            central = System.getProperty("org.bukkit.plugin.java.LibraryLoader.centralURL");
        }
        if (central == null) {
            central = "https://maven-central.storage-download.googleapis.com/maven2";
        }
        return central;
    }
}
