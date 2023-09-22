val kotlinCoroutinesVersion: String by project
val guavaVersion: String by project
val log4j2Version: String by project

dependencies {
    implementation(project(":core"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("com.google.guava:guava:$guavaVersion-jre")

    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4j2Version")

    testImplementation(kotlin("test"))
}
