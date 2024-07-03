pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("org.jetbrains.kotlin")) {
                gradle.rootProject.extra["kotlinVersion"]?.let {
                    useVersion(it as String)
                }
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "Borgar"

include(
    "app",
    "core",
    "discord",
    "messaging",
    "revolt",
    "scripts",
)
