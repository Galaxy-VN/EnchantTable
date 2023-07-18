import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

plugins {
    id("java")
    id("idea")
    id("eclipse")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.0.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.3"
}

val pluginPath = project.findProperty("enchanttable_plugin_path")
val pluginVersion: String by project
group = "io.github.galaxyvn"
version = pluginVersion

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/") // Paper
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.dmulloy2.net/repository/public/") // ProtocolLib
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots") // commandAPI snapshots
    maven("https://jitpack.io") // Obliviate Invs & SignGUI
    maven("https://repo.codemc.org/repository/maven-public/") // NBT-API
}

val commandApiVersion = "9.0.3"
dependencies {
    compileOnly("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")
    compileOnly("dev.jorel:commandapi-bukkit-shade:$commandApiVersion")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.0.0")

    compileOnly("org.projectlombok:lombok:1.18.28")

    implementation("com.github.hamza-cskn.obliviate-invs:core:4.1.13")
    implementation("com.github.hamza-cskn.obliviate-invs:advancedslot:4.1.13")
    implementation("com.github.hamza-cskn.obliviate-invs:pagination:4.1.13")
    implementation("com.github.cryptomorin:XSeries:9.4.0")
    implementation("com.github.Rapha149.SignGUI:signgui:v1.9.3")
    implementation("de.tr7zw:item-nbt-api:2.11.3")

    annotationProcessor("org.projectlombok:lombok:1.18.28")
}

tasks {

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }

    processResources {
        filesNotMatching(listOf("**/plugin.yml")) {
            expand(mapOf(project.version.toString() to pluginVersion))
        }
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        filteringCharset = Charsets.UTF_8.name()
    }

    runServer {
        minecraftVersion("1.18.2")
    }

    shadowJar {
        relocate("com.cryptomorin.xseries", "io.github.galaxyvn.shaded.xseries")
        relocate("de.tr7zw.changeme.nbtapi", "io.github.galaxyvn.shaded.nbtapi")

        manifest {
            attributes(
                mapOf(
                    "Built-By" to System.getProperty("user.name"),
                    "Version" to pluginVersion,
                    "Build-Timestamp" to SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSSZ").format(Date.from(Instant.now())),
                    "Created-By" to "Gradle ${gradle.gradleVersion}",
                    "Build-Jdk" to "${System.getProperty("java.version")} ${System.getProperty("java.vendor")} ${System.getProperty("java.vm.version")}",
                    "Build-OS" to "${System.getProperty("os.name")} ${System.getProperty("os.arch")} ${System.getProperty("os.version")}",
                    "Compiled" to (project.findProperty("enchanttable_compiled")?.toString() ?: "true").toBoolean()
                )
            )
        }
        archiveFileName.set("EnchantTable-${pluginVersion}.jar")
        minimize {
            exclude(dependency("com.github.Rapha149.SignGUI:signgui:v1.9.3"))
        }
    }

    compileJava.get().dependsOn(clean)
    build.get().dependsOn(shadowJar)
}

bukkit {
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    main = "io.github.galaxyvn.enchanttable.EnchantTable"
    version = pluginVersion
    name = "EnchantTable"
    apiVersion = "1.18"
    authors = listOf("GalaxyVN")
    depend = listOf("ProtocolLib")
    libraries = listOf("dev.jorel:commandapi-bukkit-shade:$commandApiVersion")
    permissions.create("enchanttable.command") {
        description = "Allows the player to use the /enchanttable command"
        default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.TRUE
    }
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components.getByName("java"))
        }
    }
}

if (pluginPath != null) {
    tasks {
        register<Copy>("copyJar") {
            this.doNotTrackState("Overwrites the plugin jar to allow for easier reloading")
            dependsOn(shadowJar, jar)
            from(findByName("reobfJar") ?: findByName("shadowJar") ?: findByName("jar"))
            into(pluginPath)
            doLast {
                println("Copied to plugin directory $pluginPath")
            }
        }
        named<DefaultTask>("build").get().dependsOn("copyJar")
    }
}