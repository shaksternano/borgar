val kotlinCoroutinesVersion: String by project
val jdaVersion: String by project
val discordWebhooksVersion: String by project
val exposedVersion: String by project
val postgreSqlVersion: String by project
val junitVersion: String by project

plugins {
    kotlin("plugin.serialization") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

base.archivesName.set("borgar")

dependencies {
    implementation(project(":core"))

    implementation("net.dv8tion:JDA:$jdaVersion") {
        exclude(module = "opus-java")
    }
    implementation("club.minnced:discord-webhooks:$discordWebhooksVersion")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    runtimeOnly("org.postgresql:postgresql:$postgreSqlVersion")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinCoroutinesVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
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
