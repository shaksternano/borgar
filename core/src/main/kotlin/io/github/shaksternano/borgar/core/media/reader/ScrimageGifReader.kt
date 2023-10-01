package io.github.shaksternano.borgar.core.media.reader

import com.sksamuel.scrimage.nio.AnimatedGif
import com.sksamuel.scrimage.nio.AnimatedGifReader
import com.sksamuel.scrimage.nio.ImageSource
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.ImageReaderFactory
import io.github.shaksternano.borgar.core.media.frameAtTime
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import java.io.IOException
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

class ScrimageGifReader(
    gif: AnimatedGif,
) : BaseImageReader() {

    override val frameCount: Int
    override val frameRate: Double
    override val duration: Duration
    override val frameDuration: Duration
    override val width: Int
    override val height: Int
    override val loopCount: Int
    private val frames: List<ImageFrame>

    init {
        frameCount = gif.frameCount
        if (frameCount <= 0) throw IOException("Could not read any frames")
        val frames = mutableListOf<ImageFrame>()
        duration = (0 until frameCount).fold(Duration.ZERO) { total, i ->
            val image = gif.getFrame(i).awt()
            val frameDuration = gif.getDelay(i).toKotlinDuration()
            frames.add(ImageFrame(image, frameDuration, total))
            total + frameDuration
        }
        this.frames = frames
        frameDuration = duration / frameCount
        frameRate = 1000.0 / frameDuration.inWholeMilliseconds
        val dimensions = gif.dimensions
        width = dimensions.width
        height = dimensions.height
        loopCount = gif.loopCount
    }

    private val deferredFrames: List<Deferred<ImageFrame>> by lazy {
        frames.map { CompletableDeferred(it) }
    }

    override suspend fun readFrame(timestamp: Duration): ImageFrame =
        frameAtTime(timestamp, frames, duration)

    override fun asFlow(): Flow<ImageFrame> = frames.asFlow()

    override fun createReversed(): ImageReader = Reversed(this)

    override suspend fun close() = Unit

    private class Reversed(
        private val reader: ScrimageGifReader,
    ) : ReversedImageReader(reader) {

        private val reversedFrames = buildList {
            reader.frames.reversed().fold(Duration.ZERO) { timestamp, frame ->
                add(ImageFrame(frame.content, frame.duration, timestamp))
                timestamp + frame.duration
            }
        }

        override suspend fun readFrame(timestamp: Duration): ImageFrame =
            frameAtTime(timestamp, reversedFrames, duration)

        override fun asFlow(): Flow<ImageFrame> = reversedFrames.asFlow()
    }

    object Factory : ImageReaderFactory {
        override val supportedFormats: Set<String> = setOf("gif")

        override suspend fun create(input: DataSource): ImageReader {
            val bytes = input.toByteArray()
            val imageSource = ImageSource.of(bytes)
            val gif = AnimatedGifReader.read(imageSource)
            return ScrimageGifReader(gif)
        }
    }
}
