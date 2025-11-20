package games.negative.chat.loader;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public class SpiritChatPluginLoader implements PluginLoader {

    @Override
    public void classloader(PluginClasspathBuilder builder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolver.addRepository(new RemoteRepository.Builder("central", "default", getDefaultMavenCentralMirror()).build());

        resolver.addDependency(dependency("org.springframework:spring-context:6.2.13"));

        resolver.addDependency(dependency("de.exlll:configlib-yaml:4.6.3"));

        resolver.addDependency(dependency("io.vavr:vavr:0.10.7"));

        resolver.addDependency(dependency("com.github.ben-manes.caffeine:caffeine:3.2.3"));

        builder.addLibrary(resolver);
    }

    private Dependency dependency(String coords) {
        return new Dependency(new DefaultArtifact(coords), null);
    }

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
