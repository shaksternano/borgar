val logbackVersion: String by project

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

base.archivesName.set("borgar")

dependencies {
    implementation(project(":core"))
    implementation(project(":discord"))

    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    testImplementation(kotlin("test"))
}

tasks {
    jar {
        enabled = false
    }

    shadowJar {
        archiveClassifier.set("")
        mergeServiceFiles()
        manifest {
            attributes(
                mapOf(
                    "Main-Class" to "${project.group}.Main",
                )
            )
        }
    }

    build {
        dependsOn(shadowJar)
    }
}
