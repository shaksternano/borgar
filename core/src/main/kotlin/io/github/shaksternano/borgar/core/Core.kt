package io.github.shaksternano.borgar.core

import io.github.shaksternano.borgar.core.data.connectToDatabase
import io.github.shaksternano.borgar.core.data.repository.BanRepository
import io.github.shaksternano.borgar.core.emoji.initEmojis
import io.github.shaksternano.borgar.core.graphics.registerFonts
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
    BotConfig.load(Path("config.json"))
    connectToDatabase()
    registerFonts()
    initEmojis()
    BanRepository.init()
}

private fun connectToDatabase() {
    val url = BotConfig.get().database.url.ifBlank {
        logger.warn("Database URL not found")
        return
    }
    val user = BotConfig.get().database.user
    val password = BotConfig.get().database.password
    connectToDatabase(
        url,
        user,
        password,
    )
}
