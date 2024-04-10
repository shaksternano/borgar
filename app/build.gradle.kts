plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

base.archivesName.set("borgar")

dependencies {
    api(project(":discord"))
    api(project(":revolt"))

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
                    "Main-Class" to "${project.group}.app.AppKt",
                )
            )
        }
    }

    val copyJar = register<Copy>("copyJar") {
        from(layout.buildDirectory.file("libs/${base.archivesName.get()}-$version.jar"))
        into(rootProject.layout.buildDirectory.dir("libs"))
    }

    build {
        dependsOn(shadowJar)
        finalizedBy(copyJar)
    }
}

application {
    mainClass.set("${project.group}.app.AppKt")
    tasks.run.get().workingDir = rootProject.projectDir
}
