val jdaVersion: String by project
val log4j2Version: String by project
val disruptorVersion: String by project
val guavaVersion: String by project
val gsonVersion: String by project
val commonsIoVersion: String by project
val javacvVersion: String by project
val scrimageVersion: String by project
val image4jVersion: String by project
val reflectionsVersion: String by project
val junitVersion: String by project

plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "io.github.shaksternano.borgar"
base.archivesName.set("borgar")
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.dv8tion:JDA:$jdaVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4j2Version")
    implementation("com.lmax:disruptor:$disruptorVersion")
    implementation("com.google.guava:guava:$guavaVersion-jre")
    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation("commons-io:commons-io:$commonsIoVersion")
    implementation("org.bytedeco:javacv-platform:$javacvVersion") {
        excludeJavaCpp(
            "artoolkitplus",
            "flandmark",
            "flycapture",
            "leptonica",
            "libdc1394",
            "libfreenect",
            "libfreenect2",
            "librealsense",
            "librealsense2",
            "openblas",
            "opencv",
            "tesseract",
            "videoinput",
        )
    }
    implementation("com.sksamuel.scrimage:scrimage-core:$scrimageVersion")
    implementation("net.ifok.image:image4j:$image4jVersion")
    implementation("org.reflections:reflections:$reflectionsVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

fun ModuleDependency.excludeJavaCpp(vararg modules: String) {
    modules.forEach {
        exclude(group = "org.bytedeco", module = it)
        exclude(group = "org.bytedeco", module = "$it-platform")
    }
}

tasks {
    jar {
        enabled = false
    }

    shadowJar {
        archiveClassifier.set("")
        mergeServiceFiles()
        manifest {
            attributes(mapOf(
                "Main-Class" to "${project.group}.Main",
            ))
        }
    }

    build {
        dependsOn(shadowJar)
    }

    test {
        useJUnitPlatform()
    }
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            artifact(base.archivesName.get())
            from(components["java"])
        }
    }
}
