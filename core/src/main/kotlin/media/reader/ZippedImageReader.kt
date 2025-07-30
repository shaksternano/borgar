package com.shakster.borgar.core.media.reader

import com.shakster.borgar.core.io.closeAll
import com.shakster.borgar.core.media.DualBufferedImage
import com.shakster.borgar.core.media.ImageFrame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.max
import kotlin.time.Duration

class ZippedImageReader(
    private val firstReader: ImageReader,
    private val secondReader: ImageReader,
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
        (duration / frameDuration).toInt().coerceAtLeast(1)
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

    override suspend fun readFrame(timestamp: Duration): ImageFrame {
        val firstFrame = firstReader.readFrame(timestamp)
        val secondFrame = secondReader.readFrame(timestamp)
        return firstFrame.copy(
            content = DualBufferedImage(firstFrame.content, secondFrame.content),
        )
    }

    override fun asFlow(): Flow<ImageFrame> = flow {
        val controllingReader = if (firstControlling) firstReader else secondReader
        val otherReader = if (firstControlling) secondReader else firstReader
        var totalDuration = Duration.ZERO
        // Use do-while loop in case duration is 0
        do {
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
        } while (totalDuration < otherReader.duration)
    }

    override suspend fun reversed(): ImageReader =
        ZippedImageReader(firstReader.reversed(), secondReader.reversed())

    override suspend fun createChangedSpeed(speedMultiplier: Double): ImageReader =
        ZippedImageReader(firstReader.changeSpeed(speedMultiplier), secondReader.changeSpeed(speedMultiplier))

    override suspend fun close() = closeAll(firstReader, secondReader)

    override fun toString(): String {
        return "ZippedImageReader(" +
            "firstReader=$firstReader" +
            ", secondReader=$secondReader" +
            ", firstControlling=$firstControlling" +
            ")"
    }
}
