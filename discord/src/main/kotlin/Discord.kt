package com.shakster.borgar.discord

import com.shakster.borgar.core.BotConfig
import com.shakster.borgar.core.io.DataSource
import com.shakster.borgar.core.logger
import com.shakster.borgar.discord.command.registerCommands
import com.shakster.borgar.discord.entity.DiscordMessage
import com.shakster.borgar.discord.logging.DiscordLogger
import com.shakster.borgar.messaging.BOT_STATUS
import com.shakster.borgar.messaging.event.MessageReceiveEvent
import com.shakster.borgar.messaging.util.onMessageReceived
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.generics.getChannel
import dev.minn.jda.ktx.jdabuilder.default
import dev.minn.jda.ktx.jdabuilder.intents
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.detached.IDetachableEntity
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.FileUpload
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun initDiscord(token: String) {
    val jda = default(token, enableCoroutines = true) {
        intents += GatewayIntent.MESSAGE_CONTENT
    }
    DiscordManager.create(jda)
    jda.listener<MessageReceivedEvent> {
        handleMessage(it)
    }
    jda.presence.activity = Activity.playing(BOT_STATUS)
    coroutineScope {
        launch {
            jda.registerCommands()
        }
        jda.awaitReadySuspend()
    }
    val discordLogger = jda.createDiscordLogger()
    if (discordLogger != null) {
        val logChannelId = discordLogger.logChannelId
        val logChannel = jda.getChannel<MessageChannel>(logChannelId)
        if (logChannel == null) {
            logger.warn("Discord log channel with ID $logChannelId not found!")
        } else {
            logger.info("Logging to Discord channel #${logChannel.name}!")
            logger = discordLogger
        }
    }
    logger.info("Connected to Discord")
}

private suspend fun handleMessage(event: MessageReceivedEvent) {
    onMessageReceived(event.convert())
}

private fun MessageReceivedEvent.convert(): MessageReceiveEvent {
    val message = DiscordMessage(message)
    return MessageReceiveEvent(message)
}

fun DataSource.toFileUpload(): FileUpload =
    FileUpload.fromStreamSupplier(filename) {
        newStreamBlocking()
    }

inline fun <T> IDetachableEntity.ifNotDetachedOrElse(ifDetached: T, ifNotDetached: () -> T): T =
    if (isDetached) {
        ifDetached
    } else {
        ifNotDetached()
    }

inline fun <T> IDetachableEntity.ifNotDetachedOrNull(ifNotDetached: () -> T): T? =
    ifNotDetachedOrElse(null, ifNotDetached)

private suspend fun JDA.awaitReadySuspend() {
    if (status == JDA.Status.CONNECTED) return
    var resumed = false
    val mutex = Mutex()
    suspendCoroutine { continuation ->
        listener<ReadyEvent> {
            if (resumed) return@listener
            mutex.withLock {
                @Suppress("KotlinConstantConditions")
                if (resumed) return@listener
                resumed = true
                continuation.resume(Unit)
            }
        }
    }
}

private const val DISCORD_LOG_CHANNEL_ID_ENV_VAR: String = "DISCORD_LOG_CHANNEL_ID"

private fun JDA.createDiscordLogger(): DiscordLogger? {
    val logChannelIdString = BotConfig.get().discord.logChannelId.ifBlank { return null }
    val logChannelId = logChannelIdString.toLongOrNull() ?: run {
        logger.warn("$DISCORD_LOG_CHANNEL_ID_ENV_VAR environment variable is not an integer")
        return null
    }
    return DiscordLogger(logger, logChannelId, this)
}
