plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.github.gmazzo.buildconfig") version "3.1.0"
}

group = "com.moyskleytech.mc.obsidianbb"
version = properties["CORE_VERSION"]
description = "TakstijnMinigames"
buildConfig {
    buildConfigField("String", "APP_NAME", "\"${project.name}\"")
    buildConfigField("String", "APP_VERSION", "\"${properties["CORE_VERSION"]}\"")
    buildConfigField("long", "BUILD_TIME", "${System.currentTimeMillis()}L")
}
// buildConfig {
//     buildConfigField('String', 'APP_NAME', "\"${project.name}\"")
//     buildConfigField('String', 'APP_VERSION', provider { "\"${project.version}\"" })
//     buildConfigField('long', 'BUILD_TIME', "${System.currentTimeMillis()}L")
// }

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://moyskleytech.com/debian/m2")
    maven("https://repo.dmulloy2.net/nexus/repository/public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://jitpack.io")
    maven("https://redempt.dev")
    maven("https://repo.alessiodp.com/snapshots/")
    maven("https://repo.alessiodp.com/releases/")
    maven("https://repo.screamingsandals.org/snapshots")
    maven("https://repo.maven.apache.org/maven2/")
    maven("https://nexus.iridiumdevelopment.net/repository/maven-releases/")
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.purpurmc.org/snapshots")
}

dependencies {
    // Dependencies that we want to shade in
    implementation("com.iridium:IridiumColorAPI:1.0.6")
    implementation("cloud.commandframework:cloud-minecraft-extras:" + properties["CLOUD_COMMANDS_VERSION"])
    implementation("cloud.commandframework:cloud-paper:" + properties["CLOUD_COMMANDS_VERSION"])
    implementation("cloud.commandframework:cloud-annotations:" + properties["CLOUD_COMMANDS_VERSION"])
    implementation("org.spongepowered:configurate-core:" + properties["CONFIGURATE_VERSION"])
    implementation("org.spongepowered:configurate-yaml:" + properties["CONFIGURATE_VERSION"])
    implementation("com.moyskleytech:ObsidianMaterialAPI:1.0.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")
    implementation("com.fasterxml.jackson.core:jackson-core:2.13.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.13.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.1")
    implementation("com.github.cryptomorin:XSeries:9.3.1") { isTransitive = false }

    // Other dependencies that are not required or already available at runtime
    //compileOnly("org.purpurmc.purpur:purpur-api:1.19.3-R0.1-SNAPSHOT")
    compileOnly("dev.folia:folia-api:1.20.2-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("org.projectlombok:lombok:1.18.22")

    // Enable lombok annotation processing
    annotationProcessor("org.projectlombok:lombok:1.18.22")
}

tasks {
    // "Replace" the build task with the shadowJar task (probably bad but who cares)
    assemble {
        dependsOn("shadowJar")
    }

    shadowJar {
        // Remove the archive classifier suffix
        archiveClassifier.set("")

        // Relocate dependencies
        relocate("org.spongepowered.configurate", "com.moyskleytech.mc.lib.configurate")
        relocate("org.yaml.snakeyaml", "com.moyskleytech.mc.lib.snakeyaml")
        relocate("cloud.commandframework", "com.moyskleytech.mc.lib.cloud")
        relocate("me.lucko.commodore", "com.moyskleytech.mc.lib.commodore")
        relocate("com.iridium.iridiumcolorapi", "com.moyskleytech.mc.lib.iridiumcolorapi")
        

        // Remove unnecessary files from the jar
        minimize()
    }

    // Set UTF-8 as the encoding
    compileJava {
        options.encoding = "UTF-8"
    }

    // Process Placeholders for the plugin.yml
    processResources {
        from("src/resources")
        filesMatching("**/plugin.yml") {
            expand(rootProject.project.properties)
        }

        // Always re-run this task
        outputs.upToDateWhen { false }
    }

    compileJava {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
    }

    compileTestJava {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
    }
}

// Set the Java version and vendor
java {
    toolchain {
        vendor.set(JvmVendorSpec.ADOPTOPENJDK)
    }
}
tasks.withType<JavaCompile>().configureEach {
    javaCompiler.set(javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(17))
    })
}

// Maven publishing
publishing {
    publications.create<MavenPublication>("maven") {
        setGroupId("com.moyskleytech.mc.obsidianbb")
        setArtifactId("ObsidianBB")
        setVersion("1.0.0")
        artifact(tasks["shadowJar"])
    }
}
