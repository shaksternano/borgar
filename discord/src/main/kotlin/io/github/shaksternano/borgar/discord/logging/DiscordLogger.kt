package io.github.shaksternano.borgar.discord.logging

import io.github.shaksternano.borgar.core.baseLogger
import io.github.shaksternano.borgar.core.logging.InterceptLogger
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.utils.FileUpload
import org.slf4j.Logger
import org.slf4j.event.Level
import java.io.PrintWriter
import java.io.StringWriter
import java.util.regex.Pattern

private const val BACKTICK_AND_NEWLINE_LENGTH = 8
private const val MAX_MESSAGE_LENGTH = Message.MAX_CONTENT_LENGTH - BACKTICK_AND_NEWLINE_LENGTH

class DiscordLogger(
    delegate: Logger,
    val logChannelId: Long,
    private val jda: JDA,
) : InterceptLogger(delegate) {

    override fun intercept(level: Level, message: String?, t: Throwable?, vararg arguments: Any?) {
        if (message == null) return
        val logChannel = getLogChannel() ?: return
        val messageWithArguments = formatArguments(message, arguments.asList())
        var formattedMessage = "$level - $name"
        if (messageWithArguments.isNotBlank()) {
            formattedMessage += "\n$messageWithArguments"
        }
        if (t != null) {
            val stackTrace = getStacktrace(t)
            formattedMessage += "\n\nStacktrace:\n$stackTrace"
        }
        formattedMessage = formattedMessage.trim()
        val messageAction = if (formattedMessage.length > MAX_MESSAGE_LENGTH) {
            val bytes = formattedMessage.toByteArray()
            logChannel.sendFiles(FileUpload.fromData(bytes, "message.log"))
        } else {
            logChannel.sendMessage("```\n$formattedMessage\n```")
        }
        messageAction.queue(null) {
            baseLogger.error("Failed to log message to Discord", it)
        }
    }

    private fun getLogChannel(): MessageChannel? =
        jda.getChannelById(MessageChannel::class.java, logChannelId)

    private fun formatArguments(message: String, arguments: List<Any?>): String {
        var messageWithArguments = message
        arguments.forEachIndexed { i, argument ->
            val stringArgument = argument.toString()
            messageWithArguments = messageWithArguments.replaceFirst(Pattern.quote("{}").toRegex(), stringArgument)
            messageWithArguments = messageWithArguments.replace(Pattern.quote("{$i}").toRegex(), stringArgument)
        }
        return messageWithArguments
    }

    private fun getStacktrace(t: Throwable): String {
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        t.printStackTrace(printWriter)
        return stringWriter.toString()
    }
}
