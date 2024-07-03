val ulidKotlinVersion: String by project

plugins {
    kotlin("plugin.serialization")
}

dependencies {
    api(project(":messaging"))

    implementation("com.aallam.ulid:ulid-kotlin:$ulidKotlinVersion")

    testImplementation(kotlin("test"))
}
