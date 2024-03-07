val ulidKotlinVersion: String by project

plugins {
    kotlin("plugin.serialization") version "1.9.22"
}

dependencies {
    api(project(":chat"))

    implementation("com.aallam.ulid:ulid-kotlin:$ulidKotlinVersion")

    testImplementation(kotlin("test"))
}
