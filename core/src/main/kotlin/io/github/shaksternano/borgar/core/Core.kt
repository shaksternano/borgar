package io.github.shaksternano.borgar.core

import io.github.shaksternano.borgar.core.data.connectToDatabase
import io.github.shaksternano.borgar.core.emoji.EmojiUtil
import io.github.shaksternano.borgar.core.util.Environment
import io.github.shaksternano.borgar.core.util.Fonts
import org.bytedeco.ffmpeg.global.avutil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.jvm.optionals.getOrElse

val logger: Logger = createLogger("Borgar")

fun initCore() {
    val envFileName = ".env"
    Environment.load(Path.of(envFileName))
    connectToPostgreSql()
    Fonts.registerFonts()
    EmojiUtil.initEmojiUnicodeSet()
    EmojiUtil.initEmojiShortCodesToUrlsMap()
    avutil.av_log_set_level(avutil.AV_LOG_PANIC)
}

private fun connectToPostgreSql() {
    val url = Environment.getEnvVar("POSTGRESQL_URL").getOrElse {
        logger.warn("POSTGRESQL_URL environment variable not found!")
        return
    }
    val username = Environment.getEnvVar("POSTGRESQL_USERNAME").getOrElse {
        logger.warn("POSTGRESQL_USERNAME environment variable not found!")
        return
    }
    val password = Environment.getEnvVar("POSTGRESQL_PASSWORD").getOrElse {
        logger.warn("POSTGRESQL_PASSWORD environment variable not found!")
        return
    }
    connectToDatabase(
        url,
        username,
        password,
        "org.postgresql.Driver"
    )
}

fun createLogger(name: String): Logger {
    System.setProperty("log4j2.contextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector")
    return LoggerFactory.getLogger(name)
}
