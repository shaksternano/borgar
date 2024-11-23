plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.atomicfu")
}

allprojects {
    group = "io.github.shaksternano"
    version = "1.0.0"

    applyPlugins(
        "kotlin",
        "kotlinx-serialization",
        "org.jetbrains.kotlinx.atomicfu",
    )

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

fun PluginAware.applyPlugins(vararg plugins: String) {
    plugins.forEach {
        apply(plugin = it)
    }
}

tasks {
    jar {
        enabled = false
    }
}
