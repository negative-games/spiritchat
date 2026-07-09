package gg.moonrise.chat.loader;

import gg.moonrise.engine.paper.loader.PaperPluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;

public class SpiritChatPluginLoader extends PaperPluginLoader {

    @Override
    public void addLibraries(MavenLibraryResolver resolver) {
        resolver.addDependency(dependency("io.vavr:vavr:0.10.7"));
        resolver.addDependency(dependency("com.github.ben-manes.caffeine:caffeine:3.2.3"));
    }
}
