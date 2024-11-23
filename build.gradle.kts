plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.atomicfu")
}

allprojects {
    group = "io.github.shaksternano"
    version = "1.0.0"

    apply(plugin = "kotlin")
    apply(plugin = "kotlinx-serialization")
    apply(plugin = "org.jetbrains.kotlinx.atomicfu")

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
