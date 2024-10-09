package io.github.shaksternano.borgar.discord.interaction.message

import dev.minn.jda.ktx.coroutines.await
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.deleteSilently
import io.github.shaksternano.borgar.core.task.DownloadTask
import io.github.shaksternano.borgar.core.task.FileTask
import io.github.shaksternano.borgar.core.task.run
import io.github.shaksternano.borgar.core.util.getUrls
import io.github.shaksternano.borgar.discord.DiscordManager
import io.github.shaksternano.borgar.discord.entity.DiscordGuild
import io.github.shaksternano.borgar.discord.toFileUpload
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
            event.replyFiles(uploads).await()
            DownloadResponseData(
                task = downloadTask,
                files = output,
            )
        }
    }

    override suspend fun onResponseSend(responseData: Any?, event: MessageContextInteractionEvent) {
        if (responseData !is DownloadResponseData) return
        try {
            responseData.task.close()
        } finally {
            responseData.files.forEach {
                it.path?.deleteSilently()
            }
        }
    }

    private data class DownloadResponseData(
        val task: FileTask,
        val files: List<DataSource>,
    )
}
