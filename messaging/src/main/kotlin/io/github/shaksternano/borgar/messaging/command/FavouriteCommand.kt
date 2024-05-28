package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.data.repository.SavedUrlRepository
import io.github.shaksternano.borgar.core.exception.ErrorResponseException
import io.github.shaksternano.borgar.core.graphics.configureTextDrawQuality
import io.github.shaksternano.borgar.core.graphics.drawable.Drawable
import io.github.shaksternano.borgar.core.graphics.drawable.SimpleTextDrawable
import io.github.shaksternano.borgar.core.graphics.fitFontHeight
import io.github.shaksternano.borgar.core.graphics.fitFontWidth
import io.github.shaksternano.borgar.core.io.*
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.media.*
import io.github.shaksternano.borgar.core.media.reader.ImageReader
import io.github.shaksternano.borgar.core.media.reader.LimitedDurationMediaReader
import io.github.shaksternano.borgar.core.media.reader.firstContent
import io.github.shaksternano.borgar.core.media.reader.transform
import io.github.shaksternano.borgar.core.util.asSingletonList
import io.github.shaksternano.borgar.core.util.equalsAnyIgnoreCase
import io.github.shaksternano.borgar.core.util.getUrls
import io.github.shaksternano.borgar.core.util.isTenorUrl
import io.github.shaksternano.borgar.messaging.entity.Message
import io.github.shaksternano.borgar.messaging.entity.getContent
import io.github.shaksternano.borgar.messaging.event.CommandEvent
import io.github.shaksternano.borgar.messaging.util.searchExceptSelf
import kotlinx.coroutines.flow.Flow
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.util.*
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

const val FAVOURITE_ALIAS_PREFIX: String = "favourite_"

object FavouriteCommand : NonChainableCommand() {

    override val name: String = "favourite"
    override val aliases: Set<String> = setOf("fav")
    override val description: String = "Creates an alias GIF for a file, which when sent, will be replaced by that file."
    override val argumentInfo: Set<CommandArgumentInfo<*>> = setOf(
        CommandArgumentInfo(
            key = "url",
            description = "The URL of the file to create an alias GIF of.",
            type = CommandArgumentType.String,
            required = false,
        ),
    )
    override val deferReply: Boolean = true

    override suspend fun run(arguments: CommandArguments, event: CommandEvent): List<CommandResponse> {
        val (fileUrl, downloadUrl) = getFileUrl(arguments, event)
            ?: return CommandResponse("No media found!").asSingletonList()
        val fileExtension = fileExtension(fileUrl).lowercase()
        if (fileExtension.equals("gif", ignoreCase = true) || isTenorUrl(fileUrl))
            return CommandResponse("This is already a GIF file!").asSingletonList()
        val dataSource = DataSource.fromUrl(downloadUrl)
        if (fileExtension.equalsAnyIgnoreCase("png", "jpg", "jpeg", "webp")) {
            val maxFileSize = event.getGuild()?.maxFileSize ?: event.manager.maxFileSize
            if (!dataSource.isWithinReportedSize(maxFileSize)) {
                throw ErrorResponseException("File is too large! (Max: ${toMb(maxFileSize)}MB)")
            }
            val nameWithoutExtension = filenameWithoutExtension(fileUrl)
            val result = dataSource.rename("$nameWithoutExtension.gif")
            return CommandResponse(
                files = listOf(result),
            ).asSingletonList()
        }
        return getOrCreateAliasGif(dataSource, fileUrl, event).asSingletonList()
    }

    override suspend fun onResponseSend(
        response: CommandResponse,
        responseNumber: Int,
        responseCount: Int,
        sent: Message,
        event: CommandEvent,
    ) {
        if (responseNumber != 1) return
        val responseData = response.responseData
        if (responseData !is FavouriteResponseData) return
        val attachments = sent.attachments.ifEmpty {
            logger.error("Error linking alias gif")
            event.reply("Error linking alias gif!")
            return
        }
        val url = responseData.url
        val attachment = attachments.first()
        val aliasUrl = removeQueryParams(attachment.proxyUrl ?: attachment.url)
        runCatching {
            SavedUrlRepository.createAlias(url, aliasUrl)
        }.getOrElse {
            logger.error("Error linking alias gif", it)
            event.reply("Error linking alias gif!")
        }
    }

    private suspend fun getOrCreateAliasGif(
        dataSource: UrlDataSource,
        fileUrl: String,
        event: CommandEvent,
    ): CommandResponse {
        val noQueryParams = removeQueryParams(fileUrl)
        val aliasUrl = SavedUrlRepository.readAliasUrl(noQueryParams)
        if (aliasUrl != null) {
            return CommandResponse(aliasUrl)
        }
        val maxSize = event.getGuild()?.maxFileSize ?: event.manager.maxFileSize
        val aliasGif = createAliasGif(dataSource, event, maxSize)
        if (!aliasGif.isWithinReportedSize(maxSize)) {
            aliasGif.path.deleteSilently()
            return CommandResponse("The file is too large!")
        }
        return CommandResponse(
            files = listOf(aliasGif),
            responseData = FavouriteResponseData(noQueryParams),
        )
    }

    private suspend fun getFileUrl(
        arguments: CommandArguments,
        event: CommandEvent,
    ): Pair<String, String>? {
        val messageIntersection = event.asMessageIntersection(arguments)
        val attachment = messageIntersection.attachments.firstOrNull { it.ephemeral }
        if (attachment != null) {
            return attachment.url to attachment.url
        }
        val defaultUrl = arguments.getDefaultUrl()
        if (defaultUrl != null) {
            val embedUrl = messageIntersection.getEmbeds().find {
                it.url == defaultUrl
            }?.getContent()?.url
            return defaultUrl to (embedUrl ?: defaultUrl)
        }
        return messageIntersection.searchExceptSelf {
            val attachmentUrl = it.attachments.firstOrNull()?.url
            if (attachmentUrl != null) {
                return@searchExceptSelf attachmentUrl to attachmentUrl
            }
            val contentUrl = it.content.getUrls().firstOrNull()
            if (contentUrl != null) {
                val embedUrl = it.getEmbeds().find { embed ->
                    embed.url == contentUrl
                }?.getContent()?.url
                return@searchExceptSelf contentUrl to (embedUrl ?: contentUrl)
            }
            null
        }
    }

    private suspend fun createAliasGif(
        dataSource: UrlDataSource,
        event: CommandEvent,
        maxFileSize: Long,
    ): FileDataSource {
        val url = removeQueryParams(dataSource.url)
        val encodedUrl = Base64.getEncoder().encodeToString(url.toByteArray())
        val resultName = FAVOURITE_ALIAS_PREFIX + encodedUrl
        val avatarUrl = event.manager.getSelf().effectiveAvatarUrl
        val format = fileExtension(url)
        val config = FavouriteConfig(resultName, avatarUrl, format)
        return processMedia(dataSource, config, maxFileSize)
    }
}

private class FavouriteConfig(
    override val outputName: String,
    private val avatarUrl: String,
    private val fileFormat: String,
) : MediaProcessingConfig {

    override suspend fun transformImageReader(imageReader: ImageReader, outputFormat: String): ImageReader =
        LimitedDurationMediaReader(imageReader, 5.seconds)
            .transform(FavouriteProcessor(avatarUrl, fileFormat), outputFormat)

    override fun transformOutputFormat(inputFormat: String): String = "gif"
}

private val TEXT_BOX_COLOR: Color = Color(0, 0, 0, 150)
private val TEXT_COLOR: Color = Color.WHITE

private class FavouriteProcessor(
    private val avatarUrl: String,
    private val fileFormat: String,
) : ImageProcessor<FavouriteData> {

    override suspend fun constantData(
        firstFrame: ImageFrame,
        imageSource: Flow<ImageFrame>,
        outputFormat: String,
    ): FavouriteData = createImageReader(DataSource.fromUrl(avatarUrl)).use { reader ->
        val image = firstFrame.content
        val resized = image.resize()
        val imageWidth = resized.width
        val imageHeight = resized.height
        val smallestDimension = min(imageWidth, imageHeight)
        val padding = (smallestDimension * 0.05).toInt()

        val icon = reader.firstContent()
        val iconTargetWidth = (smallestDimension * 0.2).toInt()
        val resizedIcon = icon.resizeWidth(iconTargetWidth)
        val iconWidth = resizedIcon.width
        val iconHeight = resizedIcon.height
        val iconSmallestDimension = min(iconWidth, iconHeight)
        val cornerRadius = iconSmallestDimension * 0.2
        val roundedCorners = resizedIcon.makeRoundedCorners(cornerRadius)

        val graphics = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics()
        graphics.configureTextDrawQuality()

        val formatText = SimpleTextDrawable(fileFormat.uppercase())
        val font = Font("Helvetica Neue", Font.PLAIN, smallestDimension)
        graphics.font = font
        val textBoxPadding = (iconSmallestDimension * 0.1).toInt()
        val textBoxMaxWidth = 2 * iconWidth - 2 * textBoxPadding
        val textBoxMaxHeight = iconHeight - 2 * textBoxPadding

        graphics.fitFontWidth(textBoxMaxWidth, formatText)
        graphics.fitFontHeight(textBoxMaxHeight, formatText)
        val resizedFont = graphics.font

        val textBoxWidth = formatText.getWidth(graphics) + 2 * textBoxPadding
        val textBoxHeight = formatText.getHeight(graphics) + 2 * textBoxPadding
        val textBoxX = imageWidth - padding - textBoxWidth

        val textX = textBoxX + textBoxPadding
        val textY = padding + textBoxPadding

        graphics.dispose()
        FavouriteData(
            roundedCorners,
            cornerRadius.toInt(),
            padding,
            formatText,
            resizedFont,
            textBoxX,
            textBoxWidth,
            textBoxHeight,
            textX,
            textY,
        )
    }

    override suspend fun transformImage(frame: ImageFrame, constantData: FavouriteData): BufferedImage {
        val resized = frame.content.resize()
        val graphics = resized.createGraphics()
        graphics.configureTextDrawQuality()
        val padding = constantData.padding
        graphics.drawImage(constantData.icon, padding, padding, null)
        graphics.color = TEXT_BOX_COLOR
        graphics.fillRoundRect(
            constantData.textBoxX,
            padding,
            constantData.textBoxWidth,
            constantData.textBoxHeight,
            constantData.cornerRadius,
            constantData.cornerRadius,
        )
        graphics.font = constantData.font
        graphics.color = TEXT_COLOR
        constantData.formatText.draw(
            graphics,
            constantData.textX,
            constantData.textY,
            frame.timestamp,
        )
        return resized
    }

    private fun BufferedImage.resize(): BufferedImage =
        bound(300)
}

private class FavouriteData(
    val icon: BufferedImage,
    val cornerRadius: Int,
    val padding: Int,
    val formatText: Drawable,
    val font: Font,
    val textBoxX: Int,
    val textBoxWidth: Int,
    val textBoxHeight: Int,
    val textX: Int,
    val textY: Int,
)

private class FavouriteResponseData(
    val url: String,
)
