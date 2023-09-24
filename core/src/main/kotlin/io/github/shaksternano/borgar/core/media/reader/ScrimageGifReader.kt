package io.github.shaksternano.borgar.core.media.reader

import com.sksamuel.scrimage.nio.AnimatedGifReader
import com.sksamuel.scrimage.nio.ImageSource
import io.github.shaksternano.borgar.core.collect.CloseableIterator
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.ImageReaderFactory
import io.github.shaksternano.borgar.core.media.frameAtTime
import java.io.IOException

class ScrimageGifReader(
    input: DataSource,
) : BaseImageReader() {

    override val size: Int
    override val frameRate: Double
    override val duration: Double
    override val frameDuration: Double
    override val width: Int
    override val height: Int
    override val loopCount: Int
    private val frames: MutableList<ImageFrame> = mutableListOf()

    init {
        val imageSource = ImageSource.of(input.newStreamBlocking())
        val gif = AnimatedGifReader.read(imageSource)
        size = gif.frameCount
        if (size <= 0) throw IOException("Could not read any frames")
        val totalDuration = (0 until size).fold(0.0) { total, i ->
            val image = gif.getFrame(i).awt()
            val frameDuration = gif.getDelay(i).toMillis() * 1000
            frames.add(ImageFrame(image, frameDuration.toDouble(), total))
            total + frameDuration
        }
        duration = totalDuration
        frameDuration = duration / size
        frameRate = 1000000 / frameDuration
        val dimensions = gif.dimensions
        width = dimensions.width
        height = dimensions.height
        loopCount = gif.loopCount
    }

    override fun readFrame(timestamp: Double): ImageFrame =
        frameAtTime(timestamp, frames, duration)

    override fun createReversed(): ImageReader = Reversed(this)

    override fun iterator(): CloseableIterator<ImageFrame> = CloseableIterator.wrap(frames.iterator())

    override fun close() = Unit

    private class Reversed(
        private val reader: ScrimageGifReader,
    ) : BaseImageReader() {

        override val size: Int = reader.size
        override val frameRate: Double = reader.frameRate
        override val duration: Double = reader.duration
        override val frameDuration: Double = reader.frameDuration
        override val width: Int = reader.width
        override val height: Int = reader.height
        override val loopCount: Int = reader.loopCount
        override val reversed: MediaReader<ImageFrame> = reader
        private val reversedFrames = buildList {
            reader.frames.reversed().fold(0.0) { timestamp, frame ->
                add(ImageFrame(frame.content, frame.duration, timestamp))
                timestamp + frame.duration
            }
        }

        override fun readFrame(timestamp: Double): ImageFrame =
            frameAtTime(timestamp, reversedFrames, duration)

        override fun createReversed(): ImageReader = reader

        override fun iterator(): CloseableIterator<ImageFrame> =
            CloseableIterator.wrap(reversedFrames.iterator())

        override fun close() = reader.close()
    }

    object Factory : ImageReaderFactory {
        override val supportedFormats: Set<String> = setOf("gif")

        override fun create(input: DataSource): ImageReader = ScrimageGifReader(input)
    }
}
