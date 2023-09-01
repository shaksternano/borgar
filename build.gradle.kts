plugins {
    kotlin("jvm") version "1.9.0"
}

allprojects {
    group = "io.github.shaksternano"
    version = "1.0.0"

    apply(plugin = "kotlin")

    repositories {
        mavenCentral()
    }

    tasks {
        test {
            useJUnitPlatform()
        }
    }
}
