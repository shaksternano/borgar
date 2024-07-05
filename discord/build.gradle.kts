val jdaVersion: String by project
val jdaKtxVersion: String by project

dependencies {
    api(project(":messaging"))

    implementation("com.github.freya022:JDA:2ed819ad15") {
        exclude(module = "opus-java")
    }
    implementation("com.github.minndevelopment:jda-ktx:${jdaKtxVersion}")

    testImplementation(kotlin("test"))
}
