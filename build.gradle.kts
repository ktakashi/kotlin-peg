plugins {
    kotlin("jvm") version "1.9.0"
    id("io.spring.dependency-management") version "1.1.2"
}

val kotlinVersion by extra("1.9.0")
val junitVersion by extra("5.9.3")

allprojects {
    group = group
    version = version
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "io.spring.dependency-management")
    kotlin {
        jvmToolchain(11)
    }

    dependencyManagement {
        imports {
            mavenBom("org.jetbrains.kotlin:kotlin-bom:${kotlinVersion}")
            mavenBom("org.junit:junit-bom:${junitVersion}")
        }
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()
    }
}
