package io.github.shaksternano.borgar.core.command

import com.google.common.collect.ListMultimap
import com.google.common.io.Files
import io.github.shaksternano.borgar.core.command.util.CommandResponse
import io.github.shaksternano.borgar.core.io.FileUtil
import io.github.shaksternano.borgar.core.io.NamedFile
import io.github.shaksternano.borgar.core.io.createTemporaryFile
import io.github.shaksternano.borgar.core.io.download
import io.github.shaksternano.borgar.core.media.ImageUtil
import io.github.shaksternano.borgar.core.media.MediaReaders
import io.github.shaksternano.borgar.core.media.MediaUtil
import io.github.shaksternano.borgar.core.media.imageprocessor.BasicImageProcessor
import io.github.shaksternano.borgar.core.util.DiscordUtil
import io.github.shaksternano.borgar.core.util.MessageUtil
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.awt.image.BufferedImage
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.jvm.optionals.getOrElse

object LiveReactionCommand : KotlinCommand<Path>(
    "live",
    "Live reaction meme.",
) {

    override suspend fun executeSuspend(
        arguments: List<String>,
        extraArguments: ListMultimap<String, String>,
        event: MessageReceivedEvent
    ): CommandResponse<Path> {
        event.guild.maxFileSize
        val url = MessageUtil.getUrlTenor(event.message)
            .await()
            .getOrElse {
                return CommandResponse("No media found!")
            }
        HttpClient(CIO).use { client ->
            val response = client.get(url)
            val urlNoQueryParams = url.split('?', limit = 2).first()
            val fileNameWithoutExtension = Files.getNameWithoutExtension(urlNoQueryParams)
            val extension = response.contentType()
                ?.contentSubtype
                ?: Files.getFileExtension(urlNoQueryParams)
            val path = createTemporaryFile(fileNameWithoutExtension, extension)
            download(response, path)
            val file = path.toFile()
            val fileFormat = FileUtil.getFileFormat(file)
            val imageReader = MediaReaders.createImageReader(file, fileFormat)
            val audioReader = MediaReaders.createAudioReader(file, fileFormat)
            val liveReactionReader = MediaReaders.createImageReader(
                FileUtil.getResourceInRootPackage("media/background/live_reaction.png"),
                "png",
            )
            val liveReactionImage = liveReactionReader.first().content
            val resultName = "live_reaction"
            val result = MediaUtil.processMedia(
                imageReader,
                audioReader,
                fileFormat,
                resultName,
                BasicImageProcessor {
                    drawImage(liveReactionImage, it)
                },
                DiscordUtil.getMaxUploadSize(event)
            )
            return CommandResponse<Path>(
                NamedFile(
                    result,
                    resultName,
                    fileFormat
                )
            )
                .withResponseData(result.toPath())
        }
    }

    override fun handleFirstResponse(response: Message, event: MessageReceivedEvent, responseData: Path?) {
        responseData?.deleteIfExists()
    }

    private fun drawImage(liveReactionImage: BufferedImage, image: BufferedImage): BufferedImage {
        val result = ImageUtil.copy(liveReactionImage)
        val graphics = result.createGraphics()

        val mainTopLeftX = 15
        val mainTopLeftY = 163
        val mainBottomRightX = 944
        val mainBottomRightY = 690

        val captionTopLeftX = 209
        val captionTopLeftY = 25
        val captionBottomRightX = 515
        val captionBottomRightY = 136

        val mainImage = ImageUtil.stretch(
            image,
            mainBottomRightX - mainTopLeftX,
            mainBottomRightY - mainTopLeftY,
            false
        )
        val captionImage = ImageUtil.stretch(
            image,
            captionBottomRightX - captionTopLeftX,
            captionBottomRightY - captionTopLeftY,
            false
        )

        graphics.drawImage(mainImage, mainTopLeftX, mainTopLeftY, null)
        graphics.drawImage(captionImage, captionTopLeftX, captionTopLeftY, null)

        graphics.dispose()
        return result
    }
}
