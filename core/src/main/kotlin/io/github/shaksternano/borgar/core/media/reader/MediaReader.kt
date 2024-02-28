package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.io.SuspendCloseable
import io.github.shaksternano.borgar.core.media.AudioFrame
import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.borgar.core.media.ImageProcessor
import io.github.shaksternano.borgar.core.media.VideoFrame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration

interface MediaReader<T : VideoFrame<*>> : SuspendCloseable {

    val frameCount: Int

    /**
     * The frame rate in frames per second.
     */
    val frameRate: Double

    /**
     * The total media duration.
     */
    val duration: Duration

    /**
     * The duration of each frame.
     */
    val frameDuration: Duration

    val audioChannels: Int
    val audioSampleRate: Int
    val audioBitrate: Int

    val width: Int
    val height: Int
    val loopCount: Int

    /**
     * Gets the frame at the given timestamp.
     * If the timestamp is larger than the duration of the media,
     * the reader will wrap around to the beginning.
     *
     * @param timestamp The timestamp.
     * @return The frame at the given timestamp.
     */
    suspend fun readFrame(timestamp: Duration): T

    fun asFlow(): Flow<T>

    suspend fun reversed(): MediaReader<T>

    suspend fun changeSpeed(speedMultiplier: Double): MediaReader<T>
}

typealias ImageReader = MediaReader<ImageFrame>
typealias AudioReader = MediaReader<AudioFrame>

val MediaReader<*>.isEmpty: Boolean
    get() = frameCount == 0
val MediaReader<*>.isAnimated: Boolean
    get() = frameCount > 1

suspend fun <T : VideoFrame<*>> MediaReader<T>.first(): T =
    readFrame(Duration.ZERO)

suspend fun <E, T : VideoFrame<E>> MediaReader<T>.firstContent(): E =
    first().content

suspend fun <E, T : VideoFrame<E>> MediaReader<T>.readContent(timestamp: Duration): E =
    readFrame(timestamp).content

fun ImageReader.transform(processor: ImageProcessor<*>, outputFormat: String): ImageReader =
    TransformedImageReader(this, processor, outputFormat)

private class TransformedImageReader<T>(
    private val reader: ImageReader,
    private val processor: ImageProcessor<T>,
    private val outputFormat: String,
) : BaseImageReader() {

    override val frameCount: Int = reader.frameCount
    override val frameRate: Double = reader.frameRate
    override val duration: Duration = reader.duration
    override val frameDuration: Duration = reader.frameDuration
    override val width: Int = reader.width
    override val height: Int = reader.height
    override val loopCount: Int = reader.loopCount

    override suspend fun readFrame(timestamp: Duration): ImageFrame {
        val firstFrame = reader.first()
        val constantData = processor.constantData(firstFrame, reader.asFlow(), outputFormat)
        val originalFrame =
            if (timestamp == Duration.ZERO) firstFrame
            else reader.readFrame(timestamp)
        val transformedImage = processor.transformImage(originalFrame, constantData)
        return originalFrame.copy(content = transformedImage)
    }

    override fun asFlow(): Flow<ImageFrame> = flow {
        var constantData: T? = null
        var constantDataSet = false
        val readerFlow = reader.asFlow()
        readerFlow.collect { frame ->
            if (!constantDataSet) {
                constantData = processor.constantData(frame, readerFlow, outputFormat)
                constantDataSet = true
            }
            @Suppress("UNCHECKED_CAST")
            val transformedImage = processor.transformImage(frame, constantData as T)
            emit(frame.copy(content = transformedImage))
        }
    }

    override suspend fun close() = reader.close()
}
