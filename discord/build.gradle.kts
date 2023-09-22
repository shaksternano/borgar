val jdaVersion: String by project
val jdaKtxVersion: String by project
val discordWebhooksVersion: String by project
val log4j2Version: String by project

dependencies {
    implementation(project(":core"))
    implementation(project(":chat"))

    implementation("net.dv8tion:JDA:$jdaVersion") {
        exclude(module = "opus-java")
    }
    implementation("com.github.minndevelopment:jda-ktx:${jdaKtxVersion}")
    @Suppress
    implementation("club.minnced:discord-webhooks:$discordWebhooksVersion")

    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4j2Version")

    testImplementation(kotlin("test"))
}
