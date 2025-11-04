import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.7"
    id("net.kyori.indra.git") version "3.1.3"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
}

val location = "games.negative.spiritchat"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/")

    maven("https://repo.negative.games/repository/maven-snapshots/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")

    compileOnly("io.vavr:vavr:0.10.7")

    compileOnly("de.exlll:configlib-yaml:4.6.3")

    compileOnly("com.github.ben-manes.caffeine:caffeine:3.2.3")

    implementation("games.negative.alumina:alumina:3.7.1-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

}

val targetJavaVersion = 21
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks.shadowJar {
    archiveBaseName.set(rootProject.name + "-Paper")
    archiveClassifier.set("")
    archiveVersion.set(indraGit.commit()?.name?.take(7) ?: "unknown")

    destinationDirectory.set(rootProject.rootDir.resolve("build"))

    relocate("games.negative.alumina", "$location.libs.alumina")

}

configure<PaperPluginDescription> {
    name = "SpiritChat"
    apiVersion = "1.20"
    version = indraGit.commit()?.name?.take(7) ?: "unknown"
    main = "games.negative.chat.SpiritChatPlugin"
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    loader = "games.negative.chat.loader.SpiritChatPluginLoader"

    serverDependencies {
        register("PlaceholderAPI") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
    }
}