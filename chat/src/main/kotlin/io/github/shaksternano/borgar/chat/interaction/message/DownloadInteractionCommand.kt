package io.github.shaksternano.borgar.chat.interaction.message

import io.github.shaksternano.borgar.chat.entity.Message
import io.github.shaksternano.borgar.chat.event.MessageInteractionEvent
import io.github.shaksternano.borgar.chat.interaction.InteractionResponse
import io.github.shaksternano.borgar.core.io.task.DownloadTask
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.run
import io.github.shaksternano.borgar.core.util.getUrls

object DownloadInteractionCommand : MessageInteractionCommand {

    override val name: String = "Download"
    override val deferReply: Boolean = true

    override suspend fun respond(event: MessageInteractionEvent): InteractionResponse {
        val messageContent = event.message.content
        val url = messageContent.getUrls().ifEmpty {
            return InteractionResponse("No URLs found!")
        }.first()
        val maxFileSize = event.getGuild()?.getMaxFileSize() ?: event.manager.maxFileSize
        val downloadTask = DownloadTask(
            url = url,
            maxFileSize = maxFileSize,
        )
        val output = downloadTask.run()
        return InteractionResponse(
            files = output,
            responseData = downloadTask
        )
    }

    override suspend fun onResponseSend(
        response: InteractionResponse,
        sent: Message,
        event: MessageInteractionEvent,
    ) {
        if (response.responseData !is FileTask) return
        response.responseData.close()
    }
}