plugins {
    `java-library`
    `maven-publish`
    signing
}

description = "Yet Another Kotlin Parser library"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class) {
    dokkaSourceSets {
        configureEach {
            includes.from(project.files(), "parser-combinator.md")
        }
    }
}

tasks.register<Jar>("dokkaHtmlJar") {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("html-docs")
}

tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    archiveClassifier.set("javadoc")
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
}

java {
    withSourcesJar()
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
        create<MavenPublication>("mavenJava") {
            artifactId = "parser-combinators"
            from(components["java"])
            artifact(tasks.getByName("dokkaJavadocJar"))
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
    repositories {
        maven {
            name = "sonatypeRepository"
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                if (hasProperty("ossrhUsername")) {
                    username = property("ossrhUsername") as String
                }
                if (hasProperty("ossrhPassword")) {
                    password = property("ossrhPassword") as String
                }
            }
        }
    }
}

signing {
    if (hasProperty("signing.keyId")) {
        if (hasProperty("signing.key")) {
            val keyId = property("signing.keyId") as String
            val key = property("signing.key") as String
            val password = property("signing.password") as String
            useInMemoryPgpKeys(keyId, key, password)
        }
        sign(publishing.publications["mavenJava"])
    }
}
