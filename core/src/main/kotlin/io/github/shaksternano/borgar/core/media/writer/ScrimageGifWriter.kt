package io.github.shaksternano.borgar.core.media.writer

import com.sksamuel.scrimage.DisposeMethod
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.StreamingGifWriter
import com.sksamuel.scrimage.nio.StreamingGifWriter.GifStream
import io.github.shaksternano.borgar.core.graphics.Position
import io.github.shaksternano.borgar.core.io.SuspendCloseable
import io.github.shaksternano.borgar.core.io.closeAll
import io.github.shaksternano.borgar.core.media.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Color
import java.awt.image.BufferedImage
import java.nio.file.Path
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration

private const val MAX_DIMENSION: Int = 1000
private val GIF_MINIMUM_FRAME_DURATION: Duration = 20.milliseconds

class ScrimageGifWriter(
    private val gif: GifStream,
) : NoAudioWriter() {

    override val isStatic: Boolean = false
    private var previousImage: BufferedImage? = null

    // For combining duplicate sequential frames, and dealing with the minimum frame duration.
    /**
     * The [previousImage] after transparency optimisation.
     */
    private var pendingWrite: BufferedImage? = null
    private var pendingDuration: Duration = Duration.ZERO
    private var pendingDisposeMethod: DisposeMethod = DisposeMethod.NONE

    private var closed = false

    override suspend fun writeImageFrame(frame: ImageFrame) {
        val currentImage = frame.content
            .bound(MAX_DIMENSION)
            .addTransparency()
        val previousImage1 = previousImage
        if (previousImage1 != null && isSimilar(previousImage1, currentImage)) {
            // Merge duplicate sequential frames into one.
            pendingDuration += frame.duration
        } else {
            // Optimise transparency.
            val (toWrite, disposeMethod) = if (previousImage1 == null) {
                // First frame
                currentImage to DisposeMethod.DO_NOT_DISPOSE
            } else {
                // Subsequent frames
                try {
                    optimiseTransparency(previousImage1, currentImage) to DisposeMethod.DO_NOT_DISPOSE
                } catch (e: CurrentTransparentException) {
                    pendingDisposeMethod = DisposeMethod.RESTORE_TO_BACKGROUND_COLOR
                    currentImage to DisposeMethod.RESTORE_TO_BACKGROUND_COLOR
                }
            }

            // Write the previous frame if it exists and the duration is long enough.
            val pendingWrite1 = pendingWrite
            if (pendingWrite1 != null && pendingDuration >= GIF_MINIMUM_FRAME_DURATION) {
                writeFrame(pendingWrite1, pendingDuration, pendingDisposeMethod)
                pendingWrite = null
            }

            val pendingWrite2 = pendingWrite
            if (pendingWrite2 == null) {
                previousImage = currentImage
                pendingWrite = toWrite
                pendingDuration = frame.duration
                pendingDisposeMethod = disposeMethod
            } else {
                // Handle the minimum frame duration.
                val remainingDuration = GIF_MINIMUM_FRAME_DURATION - pendingDuration
                if (remainingDuration < frame.duration) {
                    writeFrame(pendingWrite2, pendingDuration, pendingDisposeMethod)
                    previousImage = currentImage
                    pendingWrite = toWrite
                    pendingDuration = frame.duration - remainingDuration
                    pendingDisposeMethod = disposeMethod
                } else {
                    pendingDuration += frame.duration
                }
            }
        }
    }

    private fun isSimilar(image1: BufferedImage, image2: BufferedImage): Boolean =
        try {
            isFullyTransparent(optimiseTransparency(image1, image2))
        } catch (e: CurrentTransparentException) {
            false
        }

    private fun optimiseTransparency(
        previousImage: BufferedImage,
        currentImage: BufferedImage,
    ): BufferedImage {
        if (previousImage.width != currentImage.width || previousImage.height != currentImage.height) {
            throw CurrentTransparentException()
        }
        val colorTolerance = 10
        val similarPixels = mutableListOf<Position>()
        previousImage.forEachPixel { x, y ->
            val previousPixelColor = Color(previousImage.getRGB(x, y), true)
            val currentPixelColor = Color(currentImage.getRGB(x, y), true)
            if (currentPixelColor.alpha == 0 && previousPixelColor.alpha != 0) {
                // Current frame has a transparent pixel where the previous frame had an opaque pixel.
                throw CurrentTransparentException()
            } else {
                val colorDistance = previousPixelColor distanceTo currentPixelColor
                if (colorDistance <= colorTolerance) {
                    similarPixels.add(Position(x, y))
                }
            }
        }
        return removePixels(currentImage, similarPixels)
    }

    private fun removePixels(image: BufferedImage, toRemove: Iterable<Position>): BufferedImage {
        val newImage = image.copy()
        toRemove.forEach { (x, y) ->
            newImage.setRGB(x, y, 0)
        }
        return newImage
    }

    private fun filterPixels(image: BufferedImage, predicate: (Color) -> Boolean): Boolean {
        image.forEachPixel { x, y ->
            val pixelColor = Color(image.getRGB(x, y), true)
            if (!predicate(pixelColor)) {
                return false
            }
        }
        return true
    }

    private fun isFullyTransparent(image: BufferedImage): Boolean =
        filterPixels(image) { color -> color.alpha == 0 }

    private suspend fun writeFrame(image: BufferedImage, duration: Duration, disposeMethod: DisposeMethod) =
        writeFrame(gif, image, duration, disposeMethod)

    private suspend fun writeFrame(
        gif: GifStream,
        image: BufferedImage,
        duration: Duration,
        disposeMethod: DisposeMethod,
    ) {
        val immutableImage = ImmutableImage.wrapAwt(image)
        val frameDuration = if (duration > GIF_MINIMUM_FRAME_DURATION) {
            duration
        } else {
            GIF_MINIMUM_FRAME_DURATION
        }.toJavaDuration()
        withContext(Dispatchers.IO) {
            gif.writeFrame(immutableImage, frameDuration, disposeMethod)
        }
    }

    override suspend fun close() {
        if (closed) return
        closed = true
        closeAll(
            SuspendCloseable {
                pendingWrite?.let {
                    writeFrame(it, pendingDuration, pendingDisposeMethod)
                }
            },
            SuspendCloseable.fromBlocking(gif),
        )
    }

    /**
     * Indicates that the current frame had a transparent pixel
     * at a position where the previous frame has an opaque pixel,
     * which means that the current frame cannot be optimised.
     */
    private class CurrentTransparentException : Exception()

    object Factory : MediaWriterFactory {

        override val supportedFormats: Set<String> = setOf("gif")

        override suspend fun create(
            output: Path,
            outputFormat: String,
            loopCount: Int,
            audioChannels: Int,
            audioSampleRate: Int,
            audioBitrate: Int,
            maxFileSize: Long,
            maxDuration: Duration,
        ): MediaWriter {
            val infiniteLoop = loopCount == 0
            val writer = StreamingGifWriter().withInfiniteLoop(infiniteLoop)
            val gif = withContext(Dispatchers.IO) {
                writer.prepareStream(output, BufferedImage.TYPE_INT_ARGB)
            }
            return ScrimageGifWriter(gif)
        }
    }
}
