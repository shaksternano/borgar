package io.github.shaksternano.borgar.command

import com.google.common.collect.ListMultimap
import io.github.shaksternano.borgar.command.util.CommandResponse
import io.github.shaksternano.borgar.util.collect.parallelMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.net.URL
import kotlin.random.Random

private const val CAT_API_DOMAIN = "https://cataas.com"

class CatCommand(
    name: String,
    description: String,
    private val count: Int,
) : KotlinCommand<Unit>(name, description) {

    override suspend fun executeSuspend(
        arguments: List<String>,
        extraArguments: ListMultimap<String, String>,
        event: MessageReceivedEvent
    ): CommandResponse<Unit> {
        val files = (1..count).parallelMap { createCatMediaAttachment() }
        return CommandResponse(MessageCreateData.fromFiles(files))
    }

    private suspend fun createCatMediaAttachment(): FileUpload {
        val isGif = Random.nextBoolean()
        val path = if (isGif) "/cat/gif" else "/cat"
        val url = CAT_API_DOMAIN + path
        val inputStream = withContext(Dispatchers.IO) {
            URL(url).openStream()
        }
        val fileName = if (isGif) "cat.gif" else "cat.png"
        return FileUpload.fromData(inputStream, fileName)
    }
}
