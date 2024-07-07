val jdaVersion: String by project
val jdaKtxVersion: String by project

dependencies {
    api(project(":messaging"))

    implementation("com.github.freya022:JDA:76cbe5d25f") {
        exclude(module = "opus-java")
    }
    implementation("com.github.minndevelopment:jda-ktx:${jdaKtxVersion}")

    testImplementation(kotlin("test"))
}
