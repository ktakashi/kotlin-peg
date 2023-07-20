plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    kotlin("jvm") version "1.9.0"

    // Apply the java-library plugin for API and implementation separation.
    `java-library`
}

dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    testImplementation(platform(libs.junit.bom))
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    // Use the Kotlin JUnit 5 integration.
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
