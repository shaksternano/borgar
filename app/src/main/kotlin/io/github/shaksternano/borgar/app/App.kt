package io.github.shaksternano.borgar.app

import io.github.shaksternano.borgar.core.BotConfig
import io.github.shaksternano.borgar.core.START_TIME
import io.github.shaksternano.borgar.core.initCore
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.discord.initDiscord
import io.github.shaksternano.borgar.messaging.initMessaging
import io.github.shaksternano.borgar.revolt.initRevolt
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

suspend fun main() {
    logger.info("Starting")
    initCore()
    initMessaging()
    val discordToken = BotConfig.get().botTokens.discord
    val revoltToken = BotConfig.get().botTokens.revolt
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
    val timeTakenString = timeTaken.toString(DurationUnit.SECONDS, 3)
    logger.info("Finished loading in $timeTakenString")
}
