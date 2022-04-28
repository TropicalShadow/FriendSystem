plugins {
    kotlin("jvm") version "1.6.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"

}

group = "club.tesseract"
version = "0.0.1"

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.minebench.de/")
}

dependencies {
    implementation("org.reflections:reflections:0.10.2")
    implementation("de.themoep:inventorygui:1.5-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")

}


tasks {
    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        archiveFileName.set(rootProject.name + "-V" + project.version + ".jar")
        relocate("kotlin", "com.github.tropicalshadow.friendsystem.dependencies.kotlin")
        relocate("org.reflections", "com.github.tropicalshadow.friendsystem.dependencies.reflections")
        relocate("de.themoep","com.github.tropicalshadow.friendsystem.dependencies.inventorygui")
        exclude("DebugProbesKt.bin")
        exclude("META-INF/**")
    }

    processResources {
        filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to mapOf("version" to project.version))
    }

}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}