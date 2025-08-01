plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

allprojects {
    group = "com.shakster"
    version = "1.0.0"

    applyPlugins(
        "kotlin",
        "kotlinx-serialization",
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
