plugins {
    kotlin("jvm") version "1.9.0"
}

group = "io.github.ktakashi.peg"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "kotlin")
    kotlin {
        jvmToolchain(11)
    }
    tasks.named<Test>("test") {
        useJUnitPlatform()
    }
}
