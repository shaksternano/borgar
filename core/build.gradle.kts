val kotlinCoroutinesVersion: String by project
val ktorVersion: String by project
val log4j2Version: String by project
val disruptorVersion: String by project
val guavaVersion: String by project
val gsonVersion: String by project
val commonsIoVersion: String by project
val javacvVersion: String by project
val scrimageVersion: String by project
val twelveMonkeysVersion: String by project
val image4jVersion: String by project
val reflectionsVersion: String by project

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")

    api("io.ktor:ktor-client-core:$ktorVersion")
    api("io.ktor:ktor-client-cio-jvm:$ktorVersion")
    api("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    api("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    api("org.apache.logging.log4j:log4j-slf4j-impl:$log4j2Version")
    api("com.lmax:disruptor:$disruptorVersion")

    api("com.google.guava:guava:$guavaVersion-jre")
    api("com.google.code.gson:gson:$gsonVersion")
    api("commons-io:commons-io:$commonsIoVersion")

    api("org.bytedeco:javacv-platform:$javacvVersion") {
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
    api("com.sksamuel.scrimage:scrimage-core:$scrimageVersion") {
        exclude(group = "ch.qos.logback", module = "logback-classic")
        exclude(group = "ch.qos.logback", module = "logback-core")
    }
    api("com.twelvemonkeys.imageio:imageio-webp:$twelveMonkeysVersion")
    api("net.ifok.image:image4j:$image4jVersion")
    api("org.reflections:reflections:$reflectionsVersion")

    testApi(kotlin("test"))
    testApi("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinCoroutinesVersion")
}

/**
 * Exclude unused native libraries in order to reduce the JAR size.
 */
fun ModuleDependency.excludeJavaCpp(vararg modules: String) {
    modules.forEach {
        exclude(group = "org.bytedeco", module = it)
        exclude(group = "org.bytedeco", module = "$it-platform")
    }
}
