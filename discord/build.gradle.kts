val jdaVersion: String by project
val jdaKtxVersion: String by project

dependencies {
    api(project(":chat"))

    implementation("net.dv8tion:JDA:$jdaVersion") {
        exclude(module = "opus-java")
    }
    implementation("com.github.minndevelopment:jda-ktx:${jdaKtxVersion}")

    testImplementation(kotlin("test"))
}
