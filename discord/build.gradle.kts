val kotlinCoroutinesVersion: String by project
val jdaVersion: String by project
val jdaKtxVersion: String by project
val discordWebhooksVersion: String by project
val logbackVersion: String by project

dependencies {
    implementation(project(":core"))
    implementation(project(":chat"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")

    implementation("net.dv8tion:JDA:$jdaVersion") {
        exclude(module = "opus-java")
    }
    implementation("com.github.minndevelopment:jda-ktx:${jdaKtxVersion}")
    @Suppress
    implementation("club.minnced:discord-webhooks:$discordWebhooksVersion")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    testImplementation(kotlin("test"))
}
