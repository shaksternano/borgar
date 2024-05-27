val ulidKotlinVersion: String by project

plugins {
    kotlin("plugin.serialization") version "2.0.0"
}

dependencies {
    api(project(":messaging"))

    implementation("com.aallam.ulid:ulid-kotlin:$ulidKotlinVersion")

    testImplementation(kotlin("test"))
}
