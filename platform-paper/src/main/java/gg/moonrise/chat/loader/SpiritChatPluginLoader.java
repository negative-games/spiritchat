package gg.moonrise.chat.loader;

import gg.moonrise.engine.paper.loader.PaperPluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;

public class SpiritChatPluginLoader extends PaperPluginLoader {

    @Override
    public void addLibraries(MavenLibraryResolver resolver) {
        resolver.addDependency(dependency("io.vavr:vavr:0.10.7"));
        resolver.addDependency(dependency("com.zaxxer:HikariCP:7.0.2"));
        resolver.addDependency(dependency("org.xerial:sqlite-jdbc:3.50.3.0"));
        resolver.addDependency(dependency("org.mariadb.jdbc:mariadb-java-client:3.5.1"));
        resolver.addDependency(dependency("com.mysql:mysql-connector-j:9.3.0"));
        resolver.addDependency(dependency("org.postgresql:postgresql:42.7.11"));
        resolver.addDependency(dependency("com.github.ben-manes.caffeine:caffeine:3.2.3"));
    }
}
