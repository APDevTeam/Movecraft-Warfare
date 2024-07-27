plugins {
    `java-library`
    `maven-publish`
    id("io.github.0ffz.github-packages") version "1.2.1"
    id("io.papermc.hangar-publish-plugin") version "0.1.2"
}

repositories {
    gradlePluginPortal()
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven { githubPackage("apdevteam/movecraft")(this) }
    maven { githubPackage("apdevteam/movecraft-repair")(this) }
    maven { githubPackage("apdevteam/movecraft-worldguard")(this) }
    maven("https://jitpack.io")
}

dependencies {
    api("org.jetbrains:annotations-java5:24.1.0")
    compileOnly("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")
    compileOnly("net.countercraft:movecraft:+")
    compileOnly("net.countercraft.movecraft.repair:movecraft-repair:+")
    compileOnly("net.countercraft.movecraft.worldguard:movecraft-worldguard:+")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
}

group = "net.countercraft.movecraft.warfare"
version = "1.0.0_beta-4"
description = "Movecraft-Warfare"
java.toolchain.languageVersion = JavaLanguageVersion.of(17)

tasks.jar {
    archiveBaseName.set("Movecraft-Warfare")
    archiveClassifier.set("")
    archiveVersion.set("")
}

tasks.processResources {
    from(rootProject.file("LICENSE.md"))
    filesMatching("*.yml") {
        expand(mapOf("projectVersion" to project.version))
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "net.countercraft.movecraft.warfare"
            artifactId = "movecraft-warfare"
            version = "${project.version}"

            artifact(tasks.jar)
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/apdevteam/movecraft-warfare")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

hangarPublish {
    publications.register("plugin") {
        version.set(project.version as String)
        channel.set("Release")
        id.set("Airship-Pirates/Movecraft-Warfare")
        apiKey.set(System.getenv("HANGAR_API_TOKEN"))
        platforms {
            register(io.papermc.hangarpublishplugin.model.Platforms.PAPER) {
                jar.set(tasks.jar.flatMap { it.archiveFile })
                platformVersions.set(listOf("1.18.2-1.21"))
                dependencies {
                    hangar("Movecraft") {
                        required.set(true)
                    }
                    hangar("Movecraft-Repair") {
                        required.set(true)
                    }
                    hangar("Movecraft-WorldGuard") {
                        required.set(true)
                    }
                }
            }
        }
    }
}
