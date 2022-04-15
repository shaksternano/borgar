plugins {
    java
}

group = project.property("maven_group") as String
base.archivesName.set(project.property("archives_base_name") as String)
version = project.property("version") as String

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.dv8tion:JDA:${project.property("java_discord_api_version")}")
    implementation("com.google.guava:guava:${project.property("google_guava_version")}-jre")

    implementation("org.slf4j:slf4j-simple:${project.property("slf4j_version")}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${project.property("junit_version")}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
