plugins {
    `java-library`
    `maven-publish`
}

description = "Yet Another Kotlin Parser library"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.jar {
    manifest {
        attributes(
            "Created-By" to "${System.getProperties()["java.version"]} (${System.getProperties()["java.vendor"]} ${System.getProperties()["java.vm.version"]})",
            "Implementation-Title" to project.description,
            "Implementation-Version" to project.version
        )
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "parser-combinators"
            from(components["java"])
            pom {
                name.set("Kotlin PEG")
                description.set(project.description)
                url.set("https://github.com/ktakashi/kotlin-peg")
                properties.set(mapOf(
                    // TODO put kotlin version
                ))
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("ktakashi")
                        name.set("Takashi Kato")
                        email.set("ktakashi@ymail.com")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/ktakashi/kotlin-peg")
                    url.set("https://github.com/ktakashi/kotlin-peg")
                }
            }
        }
    }
}
