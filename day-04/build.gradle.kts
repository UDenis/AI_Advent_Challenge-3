import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    application
    kotlin("plugin.serialization") version "2.2.0"
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

application {
    mainClass = "ru.aiadventchallenge.MainKt"
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.coroutines.core)

    implementation(libs.koog.agents)
    implementation(libs.koog.tools)
    implementation(libs.koog.features.event.handler)

    implementation(libs.zai.sdk)

    // Ktor dependencies
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.java)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    
    runtimeOnly(libs.slf4j.simple)


    testImplementation(kotlin("test"))
}

application {
    mainClass.set("ru.aiadventchallenge.MainKt")
}

tasks.test {
    useJUnitPlatform()
}
