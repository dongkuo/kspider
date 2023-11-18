plugins {
    kotlin("jvm") version "1.9.20"
    application
}


allprojects {
    group = "site.derker"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    dependencies {
        implementation("org.slf4j:slf4j-api:2.0.9")
        implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.21.1")
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("MainKt")
}