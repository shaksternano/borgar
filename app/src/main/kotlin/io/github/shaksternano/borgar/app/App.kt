package io.github.shaksternano.borgar.app

import io.github.shaksternano.borgar.core.initCore
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.util.getEnvVar
import io.github.shaksternano.borgar.discord.initDiscord

private const val DISCORD_BOT_TOKEN_ENV_VAR = "DISCORD_BOT_TOKEN"

fun main() {
    logger.info("Starting")
    initCore()
    initDiscord(getEnvVar(DISCORD_BOT_TOKEN_ENV_VAR) ?: run {
        logger.error("$DISCORD_BOT_TOKEN_ENV_VAR environment variable not found!")
        return
    })
}
