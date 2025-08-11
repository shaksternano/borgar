package com.shakster.borgar.core

import com.shakster.borgar.core.data.connectToDatabase
import com.shakster.borgar.core.data.repository.BanRepository
import com.shakster.borgar.core.emoji.initEmojis
import com.shakster.borgar.core.graphics.registerFonts
import com.shakster.borgar.core.logging.InterceptLogger
import org.bytedeco.ffmpeg.global.avutil
import org.slf4j.LoggerFactory
import kotlin.io.path.Path
import kotlin.time.TimeSource

val START_TIME: TimeSource.Monotonic.ValueTimeMark = TimeSource.Monotonic.markNow()
val AVAILABLE_PROCESSORS: Int = Runtime.getRuntime().availableProcessors()

val logger: InterceptLogger = InterceptLogger(LoggerFactory.getLogger("Borgar"))
var ffmpegAvailable: Boolean = true
    private set

suspend fun initCore() {
    BotConfig.load(Path("config.json"))
    connectToDatabase()
    registerFonts()
    initEmojis()
    BanRepository.init()
    try {
        avutil.av_log_set_level(avutil.AV_LOG_PANIC)
    } catch (t: Throwable) {
        logger.warn("Failed to load FFmpeg, video processing will be unavailable", t)
        ffmpegAvailable = false
    }
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
