pluginManagement {
    repositories {
        gradlePluginPortal()
    }

    fun PluginResolveDetails.applyPluginVersion(property: String) {
        gradle.rootProject.extra[property]?.let {
            useVersion(it as String)
        }
    }

    resolutionStrategy {
        eachPlugin {
            val pluginId = requested.id.id
            if (pluginId.startsWith("org.jetbrains.kotlin")) {
                applyPluginVersion("kotlinVersion")
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
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
