import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.7"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
}

val identifier = "SpiritChat"
val location = "gg.moonrise.chat"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/")

    maven("https://repo.moonrise.gg/repository/maven-releases/")
    maven("https://repo.moonrise.gg/repository/maven-snapshots/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    implementation("gg.moonrise.engine:plugin-engine-paper:1.3.2-SNAPSHOT")

    compileOnly("org.springframework:spring-context:6.2.13")
    compileOnly("jakarta.annotation:jakarta.annotation-api:3.0.0")

    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    compileOnly("io.netty:netty-transport:4.1.116.Final")

    compileOnly("org.incendo:cloud-paper:2.0.0-beta.10")
    compileOnly("org.incendo:cloud-annotations:2.0.0")

    compileOnly("gg.moonrise.moss:moss-common:1.2.2")
    compileOnly("gg.moonrise.moss:moss-paper:1.2.2")

    compileOnly("io.vavr:vavr:0.10.7")

    compileOnly("de.exlll:configlib-yaml:4.8.1")

    compileOnly("com.github.ben-manes.caffeine:caffeine:3.2.3")

    compileOnly("net.luckperms:api:5.4")

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
    archiveBaseName.set(identifier + "-Paper")
    archiveClassifier.set("")
    archiveVersion.set("")

    destinationDirectory.set(rootProject.rootDir.resolve("build"))

    relocate("gg.moonrise.engine", "$location.libs.engine")

}

configure<PaperPluginDescription> {
    name = identifier
    apiVersion = "1.21"
    version = project.version.toString()
    main = "$location.SpiritChatPlugin"
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    loader = "$location.loader.SpiritChatPluginLoader"

    serverDependencies {
        register("PlaceholderAPI") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("LuckPerms") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
    }
}
