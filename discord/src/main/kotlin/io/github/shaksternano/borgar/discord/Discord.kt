package io.github.shaksternano.borgar.discord

import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.generics.getChannel
import dev.minn.jda.ktx.jdabuilder.default
import dev.minn.jda.ktx.jdabuilder.intents
import io.github.shaksternano.borgar.chat.event.MessageReceiveEvent
import io.github.shaksternano.borgar.chat.util.onMessageReceived
import io.github.shaksternano.borgar.core.BOT_STATUS
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.setLogger
import io.github.shaksternano.borgar.core.util.getEnvVar
import io.github.shaksternano.borgar.discord.entity.DiscordMessage
import io.github.shaksternano.borgar.discord.logging.DiscordLogger
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.pagination.PaginationAction
import net.dv8tion.jda.api.utils.FileUpload
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun initDiscord(token: String) {
    val jda = default(token, enableCoroutines = true) {
        intents += GatewayIntent.MESSAGE_CONTENT
    }
    DiscordManager.create(jda)
    jda.listener<MessageReceivedEvent> {
        handleMessageEvent(it)
    }
    jda.presence.activity = Activity.playing(BOT_STATUS)
    coroutineScope {
        launch {
            jda.registerSlashCommands()
        }
        launch {
            jda.awaitReadySuspend()
        }
    }
    val discordLogger = jda.createDiscordLogger()
    if (discordLogger != null) {
        val logChannelId = discordLogger.logChannelId
        val logChannel = jda.getChannel<MessageChannel>(logChannelId)
        if (logChannel == null) {
            logger.warn("Discord log channel with ID $logChannelId not found!")
        } else {
            logger.info("Logging to Discord channel #${logChannel.name}!")
            setLogger(discordLogger)
        }
    }
}

private suspend fun handleMessageEvent(event: MessageReceivedEvent) = runCatching {
    onMessageReceived(event.convert())
}.onFailure {
    logger.error("Error while handling message event", it)
}

fun MessageReceivedEvent.convert(): MessageReceiveEvent {
    val message = DiscordMessage(message)
    return MessageReceiveEvent(
        message = message,
        manager = message.manager,
    )
}

fun DataSource.toFileUpload(): FileUpload =
    FileUpload.fromStreamSupplier(filename) {
        newStreamBlocking()
    }

private suspend fun JDA.awaitReadySuspend() {
    if (status == JDA.Status.CONNECTED) return
    suspendCoroutine { continuation ->
        listener<ReadyEvent> {
            continuation.resume(Unit)
        }
    }
}

private const val DISCORD_LOG_CHANNEL_ID_ENV_VAR = "DISCORD_LOG_CHANNEL_ID"

private fun JDA.createDiscordLogger(): DiscordLogger? {
    val logChannelIdString = getEnvVar(DISCORD_LOG_CHANNEL_ID_ENV_VAR) ?: return null
    val logChannelId = logChannelIdString.toLongOrNull() ?: run {
        logger.warn("$DISCORD_LOG_CHANNEL_ID_ENV_VAR environment variable is not an integer")
        return null
    }
    return DiscordLogger(logger, logChannelId, this)
}

/**
 * Awaits the result of this RestAction
 *
 * @return Result
 */
suspend fun <T> RestAction<T>.await(): T = submit().await()

/**
 * Converts this PaginationAction to a [Flow]
 *
 * This is the same as
 * ```kotlin
 * flow {
 *   emitAll(produce())
 * }
 * ```
 *
 * @return[Flow] instance
 */
fun <T, M : PaginationAction<T, M>> M.asFlow(): Flow<T> = flow {
    cache(false)
    val queue = ArrayDeque(await())
    while (queue.isNotEmpty()) {
        while (queue.isNotEmpty()) {
            emit(queue.removeFirst())
        }
        queue.addAll(await())
    }
}
