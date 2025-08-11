@file:JvmName("Main")

package com.shakster.borgar.app

import com.shakster.borgar.core.BotConfig
import com.shakster.borgar.core.START_TIME
import com.shakster.borgar.core.initCore
import com.shakster.borgar.core.logger
import com.shakster.borgar.discord.initDiscord
import com.shakster.borgar.messaging.initMessaging
import com.shakster.borgar.revolt.initRevolt
import java.text.DecimalFormat
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

suspend fun main() {
    logger.info("Starting...")
    initCore()
    initMessaging()
    val discordToken = BotConfig.get().discord.token
    val revoltToken = BotConfig.get().revolt.token
    if (discordToken.isBlank() && revoltToken.isBlank()) {
        logger.error("No bot tokens found")
        return
    }
    if (discordToken.isNotBlank()) {
        initDiscord(discordToken)
    }
    if (revoltToken.isNotBlank()) {
        initRevolt(revoltToken)
    }
    val time = TimeSource.Monotonic.markNow()
    val timeTaken = time - START_TIME
    val seconds = timeTaken.toDouble(DurationUnit.SECONDS)
    val format = DecimalFormat("#.###")
    val timeTakenString = format.format(seconds)
    logger.info("Finished loading in ${timeTakenString}s")
}
