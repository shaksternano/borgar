plugins {
    kotlin("jvm") version "2.0.0"
}

allprojects {
    group = "io.github.shaksternano.borgar"
    version = "1.0.0"

    apply(plugin = "kotlin")

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }

    tasks {
        test {
            useJUnitPlatform()
        }
    }
}

tasks {
    jar {
        enabled = false
    }
}
