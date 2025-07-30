package com.shakster.borgar.discord.interaction.message

import com.shakster.borgar.core.io.DataSource
import com.shakster.borgar.core.io.UrlInfo
import com.shakster.borgar.core.io.deleteSilently
import com.shakster.borgar.core.task.FileTask
import com.shakster.borgar.core.task.GifTask
import com.shakster.borgar.core.util.getUrls
import com.shakster.borgar.core.util.retrieveTenorMediaUrl
import com.shakster.borgar.discord.DiscordManager
import com.shakster.borgar.discord.entity.DiscordGuild
import com.shakster.borgar.discord.entity.DiscordMessage
import com.shakster.borgar.discord.toFileUpload
import com.shakster.borgar.messaging.entity.getContent
import dev.minn.jda.ktx.coroutines.await
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
