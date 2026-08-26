plugins {
    kotlin("jvm") version "2.4.0"
    kotlin("plugin.serialization") version "2.4.0"
    application
}

group = "dev.normansanchez.androidmcp"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    val mcpVersion = "0.15.0"
    val ktorVersion = "3.5.2"

    implementation("io.ktor:ktor-client-cio:${ktorVersion}")
    implementation("io.modelcontextprotocol:kotlin-sdk:${mcpVersion}")
    implementation("io.modelcontextprotocol:kotlin-sdk-client:${mcpVersion}")
    implementation("io.ktor:ktor-server-netty:${ktorVersion}")
    implementation("io.modelcontextprotocol:kotlin-sdk-server:${mcpVersion}")

    // Kotlin PSI for symbol.find / symbol.references
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.4.0")

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("dev.normansanchez.androidmcp.server.MainKt")
}

kotlin {
    jvmToolchain(22)
}

tasks.test {
    useJUnitPlatform()
}