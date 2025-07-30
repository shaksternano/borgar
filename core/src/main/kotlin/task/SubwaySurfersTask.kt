package com.shakster.borgar.core.task

import com.shakster.borgar.core.graphics.OverlayData
import com.shakster.borgar.core.io.DataSource
import com.shakster.borgar.core.media.*
import com.shakster.borgar.core.media.reader.ImageReader
import com.shakster.borgar.core.media.reader.firstContent
import com.shakster.borgar.core.media.reader.readContent
import kotlinx.coroutines.flow.Flow
import java.awt.image.BufferedImage

class SubwaySurfersTask(
    maxFileSize: Long,
) : MediaProcessingTask(maxFileSize) {

    override val config: MediaProcessingConfig = SimpleMediaProcessingConfig(
        processor = SubwaySurfersProcessor,
    )
}

private object SubwaySurfersProcessor : ImageProcessor<SubwaySurfersData> {

    override suspend fun constantData(
        firstFrame: ImageFrame,
        imageSource: Flow<ImageFrame>,
        outputFormat: String,
    ): SubwaySurfersData {
        val dataSource = DataSource.fromResource("media/overlay/subway_surfers_gameplay.mp4")
        val subwaySurfersReader = createImageReader(dataSource)
        val image = firstFrame.content
        val width = image.width
        val height = image.height
        val resized = subwaySurfersReader.firstContent().resizeHeight(height)
        val overlayData = getOverlayData(image, resized, width, 0, true)
        return SubwaySurfersData(subwaySurfersReader, overlayData)
    }

    override suspend fun transformImage(frame: ImageFrame, constantData: SubwaySurfersData): BufferedImage {
        val subwaySurfersReader = constantData.subwaySurfersReader
        val subwaySurfersFrame = subwaySurfersReader.readContent(frame.timestamp)
        val image = frame.content
        val resized = subwaySurfersFrame.resizeHeight(image.height)
        return overlay(
            image,
            resized,
            constantData.overlayData,
            false,
        )
    }
}

private class SubwaySurfersData(
    val subwaySurfersReader: ImageReader,
    val overlayData: OverlayData,
)
