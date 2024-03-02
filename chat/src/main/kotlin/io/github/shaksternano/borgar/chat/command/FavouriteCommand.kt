package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.entity.Message
import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.chat.util.searchExceptSelf
import io.github.shaksternano.borgar.core.data.repository.SavedUrlRepository
import io.github.shaksternano.borgar.core.graphics.configureTextDrawQuality
import io.github.shaksternano.borgar.core.graphics.drawable.Drawable
import io.github.shaksternano.borgar.core.graphics.drawable.TextDrawable
import io.github.shaksternano.borgar.core.graphics.fitFontHeight
import io.github.shaksternano.borgar.core.graphics.fitFontWidth
import io.github.shaksternano.borgar.core.io.*
import io.github.shaksternano.borgar.core.io.task.ChangeExtensionTask
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.media.*
import io.github.shaksternano.borgar.core.media.reader.ImageReader
import io.github.shaksternano.borgar.core.media.reader.LimitedDurationMediaReader
import io.github.shaksternano.borgar.core.media.reader.firstContent
import io.github.shaksternano.borgar.core.media.reader.transform
import io.github.shaksternano.borgar.core.util.asSingletonList
import io.github.shaksternano.borgar.core.util.equalsAnyIgnoreCase
import io.github.shaksternano.borgar.core.util.getUrls
import kotlinx.coroutines.flow.Flow
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.util.*
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

private const val ALIAS_PREFIX: String = "favourite_"

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
        val fileUrl = getFileUrl(arguments, event)
            ?: return CommandResponse("No media found!").asSingletonList()
        val fileExtension = fileExtension(fileUrl).lowercase()
        if (fileExtension.equals("gif", ignoreCase = true))
            return CommandResponse("This is already a GIF file!").asSingletonList()
        val dataSource = DataSource.fromUrl(fileUrl)
        if (fileExtension.equalsAnyIgnoreCase("png", "jpg", "jpeg", "webp")) {
            val maxFileSize = event.getGuild()?.getMaxFileSize() ?: event.manager.maxFileSize
            val task = ChangeExtensionTask("gif", maxFileSize)
            val result = task.run(listOf(dataSource))
            return CommandResponse(
                files = result,
            ).asSingletonList()
        }
        return getOrCreateAliasGif(dataSource, event).asSingletonList()
    }

    override suspend fun onResponseSend(
        response: CommandResponse,
        responseNumber: Int,
        responseCount: Int,
        sent: Message,
        event: CommandEvent
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
        val aliasUrl = removeQueryParams(attachments.first().proxyUrl)
        runCatching {
            SavedUrlRepository.createAlias(url, aliasUrl)
        }.getOrElse {
            logger.error("Error linking alias gif", it)
            event.reply("Error linking alias gif!")
        }
    }

    private suspend fun getOrCreateAliasGif(dataSource: UrlDataSource, event: CommandEvent): CommandResponse {
        val fileUrl = removeQueryParams(dataSource.url)
        val aliasUrl = SavedUrlRepository.readAliasUrl(fileUrl)
        if (aliasUrl != null) {
            return CommandResponse(aliasUrl)
        }
        val maxSize = event.getGuild()?.getMaxFileSize() ?: event.manager.maxFileSize
        val aliasGif = createAliasGif(dataSource, event, maxSize)
        if (aliasGif.size() > maxSize) {
            aliasGif.path.deleteSilently()
            return CommandResponse("The file is too large!")
        }
        return CommandResponse(
            files = listOf(aliasGif),
            responseData = FavouriteResponseData(fileUrl),
        )
    }

    private suspend fun getFileUrl(arguments: CommandArguments, event: CommandEvent): String? {
        val messageIntersection = event.asMessageIntersection(arguments)
        val attachment = messageIntersection.attachments.firstOrNull { it.ephemeral }
        if (attachment != null) {
            return attachment.url
        }
        val defaultUrl = arguments.getDefaultUrl()
        if (defaultUrl != null) {
            return defaultUrl
        }
        return messageIntersection.searchExceptSelf {
            it.attachments.firstOrNull()?.url
                ?: it.content.getUrls().firstOrNull()
        }
    }

    private suspend fun createAliasGif(dataSource: UrlDataSource, event: CommandEvent, maxFileSize: Long): FileDataSource {
        val url = removeQueryParams(dataSource.url)
        val encodedUrl = Base64.getEncoder().encodeToString(url.toByteArray())
        val resultName = ALIAS_PREFIX + encodedUrl
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

        val formatText = TextDrawable(fileFormat.uppercase())
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

    private fun BufferedImage.resize(): BufferedImage {
        return bound(300)
    }
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
    val textY: Int
)

private class FavouriteResponseData(
    val url: String,
)