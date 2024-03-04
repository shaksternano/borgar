package io.github.shaksternano.borgar.app

import io.github.shaksternano.borgar.core.initCore
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.util.getEnvVar
import io.github.shaksternano.borgar.discord.initDiscord
import io.github.shaksternano.borgar.revolt.initRevolt

private const val DISCORD_BOT_TOKEN_ENV_VAR = "DISCORD_BOT_TOKEN"
private const val REVOLT_BOT_TOKEN_ENV_VAR = "REVOLT_BOT_TOKEN"

suspend fun main() {
    val time = System.currentTimeMillis()
    logger.info("Starting")
    initCore()
    val discordToken = getEnvVar(DISCORD_BOT_TOKEN_ENV_VAR) ?: run {
        logger.error("$DISCORD_BOT_TOKEN_ENV_VAR environment variable not found!")
        return
    }
    initDiscord(discordToken)
    val revoltToken = getEnvVar(REVOLT_BOT_TOKEN_ENV_VAR) ?: run {
        logger.error("$REVOLT_BOT_TOKEN_ENV_VAR environment variable not found!")
        return
    }
    initRevolt(revoltToken)
    val timeTaken = System.currentTimeMillis() - time
    logger.info("Finished loading in ${timeTaken}ms")
}
