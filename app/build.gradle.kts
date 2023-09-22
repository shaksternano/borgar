val log4j2Version: String by project

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

base.archivesName.set("borgar")

dependencies {
    implementation(project(":core"))
    implementation(project(":discord"))

    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4j2Version")

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
