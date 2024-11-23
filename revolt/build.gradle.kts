val ulidKotlinVersion: String by project

dependencies {
    api(project(":messaging"))

    implementation("com.aallam.ulid:ulid-kotlin:$ulidKotlinVersion")

    testImplementation(kotlin("test"))
}
