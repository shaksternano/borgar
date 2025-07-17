package io.github.shaksternano.borgar.core

import io.github.shaksternano.borgar.core.data.connectToDatabase
import io.github.shaksternano.borgar.core.data.repository.BanRepository
import io.github.shaksternano.borgar.core.emoji.initEmojis
import io.github.shaksternano.borgar.core.graphics.registerFonts
import io.github.shaksternano.borgar.core.util.getEnvVar
import io.github.shaksternano.borgar.core.util.loadEnv
import org.bytedeco.ffmpeg.global.avutil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.io.path.Path
import kotlin.time.TimeSource

val START_TIME: TimeSource.Monotonic.ValueTimeMark = TimeSource.Monotonic.markNow()
val AVAILABLE_PROCESSORS: Int = Runtime.getRuntime().availableProcessors()

val baseLogger: Logger = LoggerFactory.getLogger("Borgar")
var logger: Logger = baseLogger

suspend fun initCore() {
    avutil.av_log_set_level(avutil.AV_LOG_PANIC)
    val envFileName = ".env"
    loadEnv(Path(envFileName))
    connectToPostgreSql()
    registerFonts()
    initEmojis()
    BanRepository.init()
}

private fun connectToPostgreSql() {
    val url = getEnvVar("POSTGRESQL_URL") ?: run {
        logger.warn("POSTGRESQL_URL environment variable not found!")
        return
    }
    val username = getEnvVar("POSTGRESQL_USERNAME") ?: run {
        logger.warn("POSTGRESQL_USERNAME environment variable not found!")
        return
    }
    val password = getEnvVar("POSTGRESQL_PASSWORD") ?: run {
        logger.warn("POSTGRESQL_PASSWORD environment variable not found!")
        return
    }
    connectToDatabase(
        url,
        username,
        password,
        "org.postgresql.Driver",
    )
}
