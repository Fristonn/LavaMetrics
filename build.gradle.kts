plugins {
    java
    id("dev.arbjerg.lavalink.gradle-plugin") version "1.0.15"
}

group = "com.github.fristonn"
version = providers.gradleProperty("releaseVersion").getOrElse("1.0.0")

repositories {
    mavenCentral()
    maven("https://maven.lavalink.dev/releases")
}

lavalinkPlugin {
    name = "lavametrics"
    path = "com.github.fristonn.lavametrics"
    version = project.version.toString()
    apiVersion = "4.2.2"
    serverVersion = "4.2.2"
    configurePublishing = false
}

dependencies {
    compileOnly("dev.arbjerg.lavalink:Lavalink-Server:4.2.2")
    compileOnly("moe.kyokobot.koe:core:3.0.0-pre6")
    testImplementation("dev.arbjerg.lavalink:Lavalink-Server:4.2.2")
}

configurations.testImplementation {
    extendsFrom(configurations.compileOnly.get())
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

val verifyMetrics by tasks.registering(JavaExec::class) {
    dependsOn(tasks.testClasses)
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass = "com.github.fristonn.lavametrics.MetricsVerification"
}

tasks.check {
    dependsOn(verifyMetrics)
}
