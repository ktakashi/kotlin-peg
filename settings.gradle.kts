plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.4.0"
}

rootProject.name = "kotlin-peg"
include("parser-combinator")
include("examples:csv-parser")
