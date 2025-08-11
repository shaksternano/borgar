package com.shakster.borgar.messaging

import com.shakster.borgar.core.io.DataSource
import com.shakster.borgar.core.logging.LoggerHook
import com.shakster.borgar.messaging.entity.channel.MessageChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.event.Level
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.EmptyCoroutineContext

private const val BACKTICK_AND_NEWLINE_LENGTH: Int = 8

class MessagingAppHook(
    private val logger: Logger,
    coroutineScope: CoroutineScope = CoroutineScope(EmptyCoroutineContext),
) : LoggerHook {

    private val messageChannels: MutableList<ChannelInfo> = CopyOnWriteArrayList()
    private val pendingLogMessages: Channel<String> = Channel(Channel.UNLIMITED)

    init {
        coroutineScope.launch {
            for (message in pendingLogMessages) {
                coroutineScope {
                    for ((channelId, manager) in messageChannels) {
                        launch {
                            try {
                                val channel = manager.getChannel(channelId) as? MessageChannel ?: return@launch
                                sendMessage(message, channel)
                            } catch (t: Throwable) {
                                logger.error("Failed to send message to ${manager.platform.displayName}", t)
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun addChannel(channelId: String, manager: BotManager) {
        val logChannel = manager.getChannel(channelId)
        val platformName = manager.platform.displayName
        if (logChannel == null) {
            logger.warn("$platformName channel with ID $channelId not found")
            return
        }
        if (logChannel !is MessageChannel) {
            logger.warn("$platformName channel with ID $channelId is not a message channel")
            return
        }
        messageChannels.add(ChannelInfo(channelId, manager))
        logger.info("Logging to $platformName channel #${logChannel.name}")
    }

    override fun onLog(
        level: Level,
        message: String?,
        t: Throwable?,
        vararg arguments: Any?,
    ) {
        if (message == null) return
        val messageWithArguments = formatArguments(message, arguments.asIterable())
        var formattedMessage = "$level - ${logger.name}"
        if (messageWithArguments.isNotBlank()) {
            formattedMessage += "\n$messageWithArguments"
        }
        if (t != null) {
            val stackTrace = t.stackTraceToString()
            formattedMessage += "\n\nStacktrace:\n$stackTrace"
        }
        formattedMessage = formattedMessage.trim()
        pendingLogMessages.trySend(formattedMessage)
    }

    private fun formatArguments(message: String, arguments: Iterable<Any?>): String {
        var messageWithArguments = message
        arguments.forEachIndexed { i, argument ->
            val stringArgument = argument.toString()
            messageWithArguments = messageWithArguments.replaceFirst(Regex.fromLiteral("{}"), stringArgument)
            messageWithArguments = messageWithArguments.replace(Regex.fromLiteral("{$i}"), stringArgument)
        }
        return messageWithArguments
    }

    private suspend fun sendMessage(message: String, channel: MessageChannel) {
        val maxMessageLength = channel.manager.maxMessageContentLength - BACKTICK_AND_NEWLINE_LENGTH
        if (message.length > maxMessageLength) {
            val bytes = message.encodeToByteArray()
            channel.createMessage(DataSource.fromBytes("message.txt", bytes))
        } else {
            channel.createMessage("```\n$message\n```")
        }
    }

    private data class ChannelInfo(
        val id: String,
        val manager: BotManager,
    )
}
