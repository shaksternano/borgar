val kotlinCoroutinesVersion: String by project
val guavaVersion: String by project
val jdaVersion: String by project

dependencies {
    implementation(project(":core"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("com.google.guava:guava:$guavaVersion-jre")

    testImplementation(kotlin("test"))
}
