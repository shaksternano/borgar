plugins {
    kotlin("plugin.serialization")
}

dependencies {
    api(project(":core"))
}

tasks {
    jar {
        enabled = false
    }
}
