plugins {
    kotlin("jvm")
    application
}

dependencies {
    val ktorVersion = "2.3.9"
    implementation("org.jsoup:jsoup:1.16.1")
    implementation("org.jsoup:jsoup:1.16.2")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}