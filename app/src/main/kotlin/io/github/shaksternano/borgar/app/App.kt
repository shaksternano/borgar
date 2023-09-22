package io.github.shaksternano.borgar.app

import io.github.shaksternano.borgar.core.initCore
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.util.Environment
import io.github.shaksternano.borgar.discord.initDiscord

private const val DISCORD_BOT_TOKEN_ARGUMENT_NAME = "DISCORD_BOT_TOKEN"

fun main() {
    logger.info("Starting")
    initCore()
    initDiscord(Environment.getEnvVar(DISCORD_BOT_TOKEN_ARGUMENT_NAME).orElseThrow())
}
