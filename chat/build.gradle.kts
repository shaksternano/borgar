val kotlinCoroutinesVersion: String by project
val guavaVersion: String by project
val jdaVersion: String by project

dependencies {
    implementation(project(":core"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")

    implementation("com.google.guava:guava:$guavaVersion-jre")

    // For utility classes such as SplitUtil
    implementation("net.dv8tion:JDA:$jdaVersion") {
        exclude(module = "opus-java")
    }

    testImplementation(kotlin("test"))
}
