plugins {
    kotlin("plugin.serialization") version "2.0.0"
}

dependencies {
    api(project(":core"))
}

tasks {
    jar {
        enabled = false
    }
}
