import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = project.property("maven_group") as String
base.archivesName.set(project.property("archives_base_name") as String)
version = project.property("version") as String

repositories {
    mavenCentral()

    maven {
        name = "JitPack"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation("net.dv8tion:JDA:${project.property("java_discord_api_version")}")
    implementation("org.apache.logging.log4j:log4j-slf4j18-impl:${project.property("apache_log4j_2_version")}")
    implementation("com.lmax:disruptor:${project.property("lmax_disruptor_version")}")
    implementation("com.google.guava:guava:${project.property("google_guava_version")}-jre")
    implementation("com.google.code.gson:gson:${project.property("google_gson_version")}")
    implementation("commons-io:commons-io:${project.property("apache_commons_io_version")}")
    implementation("com.sksamuel.scrimage:scrimage-core:${project.property("scrimage_version")}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${project.property("junit_version")}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

extensions.configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("${project.property("archives_base_name")}-shadow")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "${project.group}.mediamanipulator.Main"))
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }
}

task("copyToLib") {
    doLast {
        copy {
            into("$buildDir/libs")
            from(configurations.compileClasspath)
        }
    }
}

task("stage") {
    dependsOn.add("build")
    dependsOn.add("copyToLib")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            artifact("imperishable-items-fabric")
            from(components["java"])
        }
    }
}
