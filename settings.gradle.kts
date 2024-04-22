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
