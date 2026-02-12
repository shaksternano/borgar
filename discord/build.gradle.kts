val jdaVersion: String by project
val jdaKtxVersion: String by project

dependencies {
    api(project(":messaging"))

    implementation("net.dv8tion:JDA:$jdaVersion") {
        exclude(module = "opus-java")
        exclude(module = "tink")
    }
    implementation("club.minnced:jda-ktx:$jdaKtxVersion")

    testImplementation(kotlin("test"))
}
