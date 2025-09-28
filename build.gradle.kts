plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

allprojects {
    group = "com.shakster"
    version = "1.1.0"

    applyPlugins(
        "kotlin",
        "kotlinx-serialization",
    )

    repositories {
        mavenCentral()
        google()
        maven("https://jitpack.io")
    }

    kotlin {
        jvmToolchain(21)
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
