package io.github.shaksternano.borgar.core.task

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.*
import io.github.shaksternano.borgar.core.media.reader.firstContent
import kotlinx.coroutines.flow.Flow
import java.awt.image.BufferedImage

class LiveReactionTask(
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessingConfig = SimpleMediaProcessingConfig(
        processor = LiveReactionProcessor,
    )
}

private const val MAIN_TOP_LEFT_X: Int = 15
private const val MAIN_TOP_LEFT_Y: Int = 163
private const val MAIN_BOTTOM_RIGHT_X: Int = 944
private const val MAIN_BOTTOM_RIGHT_Y: Int = 690

private const val MAIN_WIDTH: Int = MAIN_BOTTOM_RIGHT_X - MAIN_TOP_LEFT_X
private const val MAIN_HEIGHT: Int = MAIN_BOTTOM_RIGHT_Y - MAIN_TOP_LEFT_Y

private const val CAPTION_TOP_LEFT_X: Int = 209
private const val CAPTION_TOP_LEFT_Y: Int = 25
private const val CAPTION_BOTTOM_RIGHT_X: Int = 515
private const val CAPTION_BOTTOM_RIGHT_Y: Int = 136

private const val CAPTION_WIDTH: Int = CAPTION_BOTTOM_RIGHT_X - CAPTION_TOP_LEFT_X
private const val CAPTION_HEIGHT: Int = CAPTION_BOTTOM_RIGHT_Y - CAPTION_TOP_LEFT_Y

private object LiveReactionProcessor : ImageProcessor<LiveReactionData> {

    override suspend fun constantData(
        firstFrame: ImageFrame,
        imageSource: Flow<ImageFrame>,
        outputFormat: String,
    ): LiveReactionData {
        val dataSource = DataSource.fromResource("media/background/live_reaction.png")
        val liveReactionImage = createImageReader(dataSource).firstContent()
        return LiveReactionData(liveReactionImage)
    }

    override suspend fun transformImage(frame: ImageFrame, constantData: LiveReactionData): BufferedImage {
        val image = frame.content
        val liveReactionImage = constantData.liveReactionImage
        val result = liveReactionImage.copy()
        val graphics = result.createGraphics()
        val mainImage = image.stretch(
            MAIN_WIDTH,
            MAIN_HEIGHT,
        )
        val captionImage = image.stretch(
            CAPTION_WIDTH,
            CAPTION_HEIGHT,
        )
        graphics.drawImage(mainImage, MAIN_TOP_LEFT_X, MAIN_TOP_LEFT_Y, null)
        graphics.drawImage(captionImage, CAPTION_TOP_LEFT_X, CAPTION_TOP_LEFT_Y, null)
        graphics.dispose()
        return result
    }
}

@JvmInline
private value class LiveReactionData(
    val liveReactionImage: BufferedImage,
)
