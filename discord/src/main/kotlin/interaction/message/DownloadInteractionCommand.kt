package com.shakster.borgar.discord.interaction.message

import com.shakster.borgar.core.io.DataSource
import com.shakster.borgar.core.io.deleteSilently
import com.shakster.borgar.core.task.DownloadTask
import com.shakster.borgar.core.task.FileTask
import com.shakster.borgar.core.task.run
import com.shakster.borgar.core.util.getUrls
import com.shakster.borgar.discord.DiscordManager
import com.shakster.borgar.discord.entity.DiscordGuild
import com.shakster.borgar.discord.toFileUpload
import dev.minn.jda.ktx.coroutines.await
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent

object DownloadInteractionCommand : DiscordMessageInteractionCommand {

    override val name: String = "Download"

    override suspend fun respond(event: MessageContextInteractionEvent): Any? {
        val messageContent = event.target.contentRaw
        val url = messageContent.getUrls().firstOrNull() ?: run {
            event.reply("No URLs found!")
                .setEphemeral(true)
                .await()
            return null
        }
        return coroutineScope {
            val deferJob = launch {
                event.deferReply().await()
            }
            val maxFileSize = event.guild?.let { DiscordGuild(it) }?.maxFileSize ?: DiscordManager[event.jda].maxFileSize
            val downloadTask = DownloadTask(
                url = url,
                maxFileSize = maxFileSize,
            )
            val output = downloadTask.run()
            val uploads = output.map { it.toFileUpload() }
            deferJob.join()
            event.hook.sendFiles(uploads).await()
            ResponseData(
                task = downloadTask,
                files = output,
            )
        }
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
