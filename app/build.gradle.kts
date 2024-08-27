plugins {
    id("com.gradleup.shadow") version "8.3.0"
    application
}

base.archivesName.set("borgar")

dependencies {
    api(project(":discord"))
    api(project(":revolt"))

    testImplementation(kotlin("test"))
}

val mainClassFullName = "${project.group}.borgar.app.AppKt"

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
                    "Main-Class" to mainClassFullName,
                )
            )
        }
        dependsOn(distTar, distZip)
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
    mainClass.set(mainClassFullName)
    tasks.run.get().workingDir = rootProject.projectDir
}
