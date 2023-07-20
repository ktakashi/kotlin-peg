
group = "io.github.ktakashi.peg.examples"

dependencies {
    implementation(project(":parser-combinator"))
    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
}
