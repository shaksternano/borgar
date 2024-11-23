pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            fun applyPluginVersion(property: String) {
                gradle.rootProject.extra[property]?.let {
                    useVersion(it as String)
                }
            }

            val pluginId = requested.id.id
            if (pluginId == "org.jetbrains.kotlinx.atomicfu") {
                applyPluginVersion("kotlinxAtomicFuVersion")
            } else if (pluginId.startsWith("org.jetbrains.kotlin")) {
                applyPluginVersion("kotlinVersion")
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
