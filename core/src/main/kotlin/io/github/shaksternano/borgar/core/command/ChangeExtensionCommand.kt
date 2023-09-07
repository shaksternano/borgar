package io.github.shaksternano.borgar.core.command

import com.google.common.collect.ListMultimap
import com.google.common.io.Files
import io.github.shaksternano.borgar.core.command.util.CommandResponse
import io.github.shaksternano.borgar.core.util.MessageUtil
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.io.InputStream
import kotlin.jvm.optionals.getOrElse

class ChangeExtensionCommand(
    private val newExtension: String
) : KotlinCommand<Unit>(
    "${newExtension}2",
    "Changes the extension of a file to `.$newExtension`.",
) {

    override suspend fun executeSuspend(
        arguments: List<String>,
        extraArguments: ListMultimap<String, String>,
        event: MessageReceivedEvent
    ): CommandResponse<Unit> {
        val url = MessageUtil.getUrlTenor(event.message)
            .await()
            .getOrElse {
                return CommandResponse("No media found!")
            }
        val urlNoQueryParams = url.split('?').first()
        val oldExtension = Files.getFileExtension(urlNoQueryParams)
        if (oldExtension == newExtension) {
            return CommandResponse("File already has the extension `.$newExtension`!")
        }
        HttpClient(CIO).use { client ->
            val response = client.get(url)
            val contentLength = response.contentLength() ?: 0
            if (contentLength > Message.MAX_FILE_SIZE) {
                return CommandResponse("File is too large!")
            }
            val fileNameWithoutExtension = Files.getNameWithoutExtension(urlNoQueryParams)
            val fileName =
                if (newExtension.isBlank()) fileNameWithoutExtension
                else "$fileNameWithoutExtension.$newExtension"
            val inputStream = response.body<InputStream>()
            return CommandResponse(
                inputStream,
                fileName
            )
        }
    }
}
