plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation("org.jsoup:jsoup:1.16.1")
    implementation("org.jsoup:jsoup:1.16.2")
    implementation("io.ktor:ktor-client-core:2.3.6")
    implementation("io.ktor:ktor-client-okhttp:2.3.6")
    implementation("io.ktor:ktor-client-java:2.3.6")
}

tasks.test {
    useJUnitPlatform()
}