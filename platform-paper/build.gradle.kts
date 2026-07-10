import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.7"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
    id("com.modrinth.minotaur") version "2.9.0"
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

    compileOnly("com.zaxxer:HikariCP:7.0.2")

    compileOnly("org.incendo:cloud-paper:2.0.0-beta.10")
    compileOnly("org.incendo:cloud-annotations:2.0.0")

    compileOnly("gg.moonrise.moss:moss-common:1.2.2")
    compileOnly("gg.moonrise.moss:moss-paper:1.2.2")

    compileOnly("de.exlll:configlib-yaml:4.8.1")

    compileOnly("com.github.ben-manes.caffeine:caffeine:3.2.3")

    compileOnly("net.luckperms:api:5.4")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    testImplementation("org.junit.jupiter:junit-jupiter:5.14.4")
    testImplementation("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    testImplementation("de.exlll:configlib-yaml:4.8.1")
    testCompileOnly("gg.moonrise.moss:moss-common:1.2.2")
    testRuntimeOnly("gg.moonrise.moss:moss-common:1.2.2")
    testRuntimeOnly("com.github.ben-manes.caffeine:caffeine:3.2.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.14.4")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    test {
        useJUnitPlatform()
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

val modrinthGameVersions = providers.environmentVariable("MODRINTH_GAME_VERSIONS")
    .map { versions -> versions.split(",").map { it.trim() }.filter { it.isNotEmpty() } }
    .orElse(listOf("1.21.8"))

val modrinthLoaders = providers.environmentVariable("MODRINTH_LOADERS")
    .map { loaders -> loaders.split(",").map { it.trim() }.filter { it.isNotEmpty() } }
    .orElse(listOf("paper"))

modrinth {
    token.set(providers.environmentVariable("MODRINTH_TOKEN"))
    projectId.set(providers.environmentVariable("MODRINTH_PROJECT_ID").orElse("spiritchat"))
    versionNumber.set(providers.environmentVariable("MODRINTH_VERSION_NUMBER").orElse(version.toString()))
    versionName.set(providers.environmentVariable("MODRINTH_VERSION_NAME").orElse("$identifier ${version}"))
    versionType.set(providers.environmentVariable("MODRINTH_VERSION_TYPE").orElse("release"))
    uploadFile.set(tasks.shadowJar)
    gameVersions.addAll(modrinthGameVersions)
    loaders.addAll(modrinthLoaders)
    changelog.set(providers.environmentVariable("MODRINTH_CHANGELOG").orElse("Automated release."))
}

tasks.named("modrinth") {
    dependsOn(tasks.shadowJar)
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
            required = true
        }
        register("LuckPerms") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
    }
}
