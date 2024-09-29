package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.AVAILABLE_PROCESSORS
import io.github.shaksternano.borgar.core.io.SuspendCloseable
import io.github.shaksternano.borgar.core.io.closeAll
import io.github.shaksternano.borgar.core.media.*
import io.github.shaksternano.gifcodec.AsyncExecutor
import io.github.shaksternano.gifcodec.use
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration

interface MediaReader<T : VideoFrame<*>> : SuspendCloseable {

    val frameCount: Int

    /**
     * The duration of each frame.
     */
    val frameDuration: Duration

    /**
     * The total media duration.
     */
    val duration: Duration

    /**
     * The frame rate in frames per second.
     */
    val frameRate: Double

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

fun ImageReader.transform(
    processor: ImageProcessor<*>,
    outputFormat: String,
    loopCount: Int = this.loopCount,
): ImageReader {
    return if (processor is IdentityImageProcessor) {
        this
    } else if (this is TransformedImageReader<*>) {
        then(processor, loopCount)
    } else {
        TransformedImageReader(
            this,
            processor,
            outputFormat,
            loopCount,
        )
    }
}

private class TransformedImageReader<T : Any>(
    private val reader: ImageReader,
    private val processor: ImageProcessor<T>,
    private val outputFormat: String,
    override val loopCount: Int,
) : BaseImageReader() {

    override val frameCount: Int = reader.frameCount
    override val frameRate: Double = reader.frameRate
    override val duration: Duration = reader.duration
    override val frameDuration: Duration = reader.frameDuration
    override val width: Int = reader.width
    override val height: Int = reader.height

    private val flow: Flow<ImageFrame> = reader.asFlow()
    private val mutex: Mutex = Mutex()
    private lateinit var constantData: T

    override suspend fun readFrame(timestamp: Duration): ImageFrame {
        val originalFrame = reader.readFrame(timestamp)
        initConstantData(originalFrame)
        val transformedImage = processor.transformImage(originalFrame, constantData)
        return originalFrame.copy(content = transformedImage)
    }

    override fun asFlow(): Flow<ImageFrame> = channelFlow {
        AsyncExecutor<ImageFrame, ImageFrame>(
            maxConcurrency = AVAILABLE_PROCESSORS,
            task = {
                initConstantData(it)
                val transformedImage = processor.transformImage(it, constantData)
                it.copy(content = transformedImage)
            },
            onOutput = {
                send(it)
            },
        ).use { executor ->
            flow.collect {
                executor.submit(it)
            }
        }
    }

    private suspend fun initConstantData(frame: ImageFrame) {
        if (::constantData.isInitialized) return
        mutex.withLock {
            if (!::constantData.isInitialized) {
                constantData = processor.constantData(frame, flow, outputFormat)
            }
        }
    }

    fun then(after: ImageProcessor<*>, loopCount: Int): TransformedImageReader<*> {
        return if (after is IdentityImageProcessor) {
            this
        } else {
            TransformedImageReader(reader, processor then after, outputFormat, loopCount)
        }
    }

    override suspend fun close() = closeAll(
        reader,
        processor,
    )

    override fun toString(): String {
        return "TransformedImageReader(" +
            "reader=$reader" +
            ", processor=$processor" +
            ", outputFormat='$outputFormat'" +
            ", mutex=$mutex" +
            ", constantData=${
                if (::constantData.isInitialized) constantData.toString()
                else "[not initialized]"
            }" +
            ")"
    }
}
