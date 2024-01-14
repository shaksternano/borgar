val kotlinCoroutinesVersion: String by project
val guavaVersion: String by project
val logbackVersion: String by project

dependencies {
    implementation(project(":core"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("com.google.guava:guava:$guavaVersion-jre")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    testImplementation(kotlin("test"))
}
