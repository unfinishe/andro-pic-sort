plugins {
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
}

tasks.withType<org.gradle.api.tasks.compile.JavaCompile>().configureEach {
    enabled = false
}

tasks.test {
    useJUnitPlatform()
}

