plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.atomicfu")
}

allprojects {
    group = "com.shakster"
    version = "1.0.0"

    applyPlugins(
        "kotlin",
        "kotlinx-serialization",
        "org.jetbrains.kotlinx.atomicfu",
    )

    repositories {
        mavenCentral()
        maven("https://central.sonatype.com/repository/maven-snapshots")
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
