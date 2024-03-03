plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "Borgar"

include(
    "app",
    "chat",
    "core",
    "discord",
    "scripts",
)
