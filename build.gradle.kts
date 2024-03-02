plugins {
    kotlin("jvm") version "1.9.22"
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
