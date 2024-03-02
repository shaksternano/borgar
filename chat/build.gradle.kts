val kotlinCoroutinesVersion: String by project
val guavaVersion: String by project
val logbackVersion: String by project

dependencies {
    api(project(":core"))

    testImplementation(kotlin("test"))
}
