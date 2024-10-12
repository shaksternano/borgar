package io.github.shaksternano.borgar.discord.interaction.message

import dev.minn.jda.ktx.coroutines.await
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.UrlInfo
import io.github.shaksternano.borgar.core.io.deleteSilently
import io.github.shaksternano.borgar.core.task.FileTask
import io.github.shaksternano.borgar.core.task.GifTask
import io.github.shaksternano.borgar.core.util.getUrls
import io.github.shaksternano.borgar.core.util.retrieveTenorMediaUrl
import io.github.shaksternano.borgar.discord.DiscordManager
import io.github.shaksternano.borgar.discord.entity.DiscordGuild
import io.github.shaksternano.borgar.discord.entity.DiscordMessage
import io.github.shaksternano.borgar.discord.toFileUpload
import io.github.shaksternano.borgar.messaging.entity.getContent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent

object GifInteractionCommand : DiscordMessageInteractionCommand {

    override val name: String = "Convert to GIF"

    override suspend fun respond(event: MessageContextInteractionEvent): Any? {
        val message = event.target
        val input = getFileUrl(message)?.asDataSource() ?: run {
            event.reply("No files found!")
                .setEphemeral(true)
                .await()
            return null
        }
        return coroutineScope {
            val deferJob = launch {
                event.deferReply().await()
            }
            val maxFileSize = event.guild?.let { DiscordGuild(it) }?.maxFileSize ?: DiscordManager[event.jda].maxFileSize
            val task = GifTask(
                forceTranscode = false,
                forceRename = false,
                maxFileSize = maxFileSize,
            )
            val output = task.run(listOf(input))
            val uploads = output.map { it.toFileUpload() }
            deferJob.join()
            event.hook.sendFiles(uploads).await()
            ResponseData(
                task = task,
                files = output,
            )
        }
    }

    private suspend fun getFileUrl(message: Message): UrlInfo? {
        val attachment = message.attachments.firstOrNull()
        if (attachment != null) {
            return UrlInfo(attachment.url)
        }
        val url = message.contentRaw.getUrls().firstOrNull() ?: return null
        val embed = DiscordMessage(message).getEmbeds().firstOrNull { it.url == url }
        val embedContent = embed?.getContent(getGif = false)
        if (embedContent != null) {
            return embedContent
        }
        return retrieveTenorMediaUrl(url, getGif = false) ?: UrlInfo(url)
    }

    override suspend fun onResponseSend(responseData: Any?, event: MessageContextInteractionEvent) {
        if (responseData !is ResponseData) return
        try {
            responseData.task.close()
        } finally {
            responseData.files.forEach {
                it.path?.deleteSilently()
            }
        }
    }

    private data class ResponseData(
        val task: FileTask,
        val files: List<DataSource>,
    )
}
