package io.github.shaksternano.borgar.core.util

import io.github.shaksternano.borgar.core.io.IO_DISPATCHER
import io.github.shaksternano.borgar.core.logger
import kotlinx.coroutines.withContext
import java.nio.file.Path
import kotlin.io.path.forEachLine
import kotlin.system.exitProcess

private val customEnvVars: MutableMap<String, String> = HashMap()

suspend fun loadEnv(path: Path) {
    try {
        withContext(IO_DISPATCHER) {
            path.forEachLine {
                if (it.isNotBlank()) {
                    val envVar = it.split("=", limit = 2)
                    if (envVar.size == 2) {
                        val key = envVar[0].trim()
                        val value = envVar[1].trim()
                        if (key.isNotBlank() && value.isNotBlank()) {
                            setEnvVar(key, value)
                        }
                    } else {
                        logger.error("Invalid environment variable: $it")
                    }
                }
            }
        }
    } catch (t: Throwable) {
        logger.error("Error reading environment variables from ${path.toAbsolutePath()}", t)
        exitProcess(1)
    }
}

fun getEnvVar(key: String): String? =
    customEnvVars[key] ?: System.getenv(key)

fun setEnvVar(key: String, value: String) {
    customEnvVars[key] = value
}
