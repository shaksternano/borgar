package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.io.closeAll
import io.github.shaksternano.borgar.core.media.DualBufferedImage
import io.github.shaksternano.borgar.core.media.ImageFrame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.max
import kotlin.time.Duration

class ZippedImageReader(
    private val firstReader: MediaReader<ImageFrame>,
    private val secondReader: MediaReader<ImageFrame>,
) : BaseImageReader() {

    private val firstControlling: Boolean =
        firstReader.isAnimated &&
            (!secondReader.isAnimated || firstReader.frameDuration <= secondReader.frameDuration)
    override val frameRate: Double = ifFirstControllingOrElse(firstReader.frameRate, secondReader.frameRate)
    override val duration: Duration = ifEmptyOrElse(Duration.ZERO) {
        if (firstReader.duration > secondReader.duration) firstReader.duration
        else secondReader.duration
    }
    override val frameDuration: Duration =
        ifFirstControllingOrElse(firstReader.frameDuration, secondReader.frameDuration)
    override val frameCount: Int = ifEmptyOrElse(0) {
        (duration / frameDuration).toInt()
    }
    override val width: Int = ifFirstControllingOrElse(firstReader.width, secondReader.width)
    override val height: Int = ifFirstControllingOrElse(firstReader.height, secondReader.height)
    override val loopCount: Int =
        if (firstReader.loopCount == 0 || secondReader.loopCount == 0) 0
        else max(firstReader.loopCount, secondReader.loopCount)

    private fun <T> ifEmptyOrElse(ifEmpty: T, ifNotEmpty: () -> T): T =
        if (firstReader.isEmpty || secondReader.isEmpty) ifEmpty
        else ifNotEmpty()

    private fun <T> ifFirstControllingOrElse(ifFirstControlling: T, ifSecondControlling: T): T =
        if (firstControlling) ifFirstControlling
        else ifSecondControlling

    override suspend fun readFrame(timestamp: Duration): ImageFrame = firstReader.readFrame(timestamp)

    suspend fun readFrame2(timestamp: Duration): ImageFrame = secondReader.readFrame(timestamp)

    override fun asFlow(): Flow<ImageFrame> = flow {
        val controllingReader = if (firstControlling) firstReader else secondReader
        val otherReader = if (firstControlling) secondReader else firstReader
        var totalDuration = Duration.ZERO
        while (totalDuration < otherReader.duration) {
            controllingReader.asFlow().collect {
                val timestamp = it.timestamp + totalDuration
                val other = otherReader.readFrame(timestamp)
                val firstFrame = (if (firstControlling) it else other)
                val secondFrame = (if (firstControlling) other else it)
                val zippedFrame = it.copy(
                    content = DualBufferedImage(firstFrame.content, secondFrame.content),
                    timestamp = timestamp,
                )
                emit(zippedFrame)
            }
            totalDuration += controllingReader.duration
        }
    }

    override fun createReversed(): ImageReader =
        ZippedImageReader(firstReader.reversed, secondReader.reversed)

    override suspend fun close() = closeAll(firstReader, secondReader)
}
