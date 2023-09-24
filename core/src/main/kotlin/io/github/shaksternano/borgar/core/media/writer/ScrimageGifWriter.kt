package io.github.shaksternano.borgar.core.media.writer

import com.sksamuel.scrimage.DisposeMethod
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.StreamingGifWriter.GifStream
import io.github.shaksternano.borgar.core.media.*
import java.awt.Color
import java.awt.image.BufferedImage
import java.nio.file.Path
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.math.max

private const val GIF_MINIMUM_FRAME_DURATION = 20000
private const val MAX_DIMENSION = 500

class ScrimageGifWriter(
    output: Path,
    loopCount: Int,
) : NoAudioWriter() {

    private val gif: GifStream

    init {
        val infiniteLoop = loopCount == 0
        val writer = FixedLoopingGifWriter().withInfiniteLoop(infiniteLoop)
        gif = writer.prepareStream(output, BufferedImage.TYPE_INT_ARGB)
    }

    override val isStatic: Boolean = false
    private var previousImage: BufferedImage? = null
    private var cannotOptimiseNext: Boolean = false

    // For combining duplicate sequential frames, and dealing with the minimum frame duration.
    /**
     * The [previousImage] after transparency optimisation.
     */
    private var pendingWrite: BufferedImage? = null
    private var pendingDuration: Double = 0.0
    private var pendingDisposeMethod: DisposeMethod = DisposeMethod.NONE

    private var closed = false

    override fun writeImageFrame(frame: ImageFrame) {
        val currentImage = frame.content
            .bound(MAX_DIMENSION)
            .convertType(BufferedImage.TYPE_INT_ARGB)
        val previousImage = previousImage
        if (previousImage != null && isSimilar(previousImage, currentImage)) {
            // Merge duplicate sequential frames into one.
            pendingDuration += frame.duration
        } else {
            // Write the previous frame if it exists and the duration is long enough.
            if (pendingWrite != null && pendingDuration >= GIF_MINIMUM_FRAME_DURATION) {
                writeFrame()
                pendingWrite = null
            }

            // Optimise transparency.
            var toWrite: BufferedImage
            var disposeMethod: DisposeMethod
            if (previousImage == null) {
                toWrite = currentImage
                disposeMethod = DisposeMethod.NONE
            } else if (cannotOptimiseNext) {
                toWrite = currentImage
                if (isFullyOpaque(currentImage)) {
                    disposeMethod = DisposeMethod.NONE
                    cannotOptimiseNext = false
                } else {
                    disposeMethod = DisposeMethod.RESTORE_TO_BACKGROUND_COLOR
                }
            } else {
                try {
                    toWrite = optimiseTransparency(previousImage, currentImage)
                    disposeMethod = DisposeMethod.DO_NOT_DISPOSE
                } catch (e: PreviousTransparentException) {
                    toWrite = currentImage
                    disposeMethod = DisposeMethod.RESTORE_TO_BACKGROUND_COLOR
                    cannotOptimiseNext = true
                }
            }
            val frameDuration = frame.duration
            if (pendingWrite == null) {
                this.previousImage = currentImage
                pendingWrite = toWrite
                pendingDuration = frameDuration
                pendingDisposeMethod = disposeMethod
            } else {
                // Handle the minimum frame duration.
                val remainingDuration = GIF_MINIMUM_FRAME_DURATION - pendingDuration
                if (remainingDuration < frameDuration) {
                    writeFrame()
                    this.previousImage = currentImage
                    pendingWrite = toWrite
                    pendingDuration = frameDuration - remainingDuration
                    pendingDisposeMethod = disposeMethod
                } else {
                    pendingDuration += frameDuration
                }
            }
        }
    }

    private fun isSimilar(image1: BufferedImage, image2: BufferedImage): Boolean {
        return try {
            isFullyTransparent(optimiseTransparency(image1, image2))
        } catch (e: PreviousTransparentException) {
            false
        }
    }

    private fun optimiseTransparency(
        previousImage: BufferedImage,
        currentImage: BufferedImage
    ): BufferedImage {
        if (previousImage.width != currentImage.width || previousImage.height != currentImage.height) {
            throw PreviousTransparentException()
        }
        val colorTolerance = 10
        val similarPixels: MutableList<Position> = mutableListOf()
        for (x in 0 until previousImage.width) {
            for (y in 0 until previousImage.height) {
                val previousPixelColor = Color(previousImage.getRGB(x, y), true)
                val currentPixelColor = Color(currentImage.getRGB(x, y), true)
                if (currentPixelColor.alpha == 0 && previousPixelColor.alpha != 0) {
                    throw PreviousTransparentException()
                } else {
                    val colorDistance = ImageUtil.colorDistance(previousPixelColor, currentPixelColor)
                    if (colorDistance <= colorTolerance) {
                        similarPixels.add(Position(x, y))
                    }
                }
            }
        }
        return removePixels(currentImage, similarPixels)
    }

    private fun removePixels(image: BufferedImage, toRemove: Iterable<Position>): BufferedImage {
        val newImage = ImageUtil.copy(image)
        toRemove.forEach { (x, y) ->
            newImage.setRGB(x, y, 0)
        }
        return newImage
    }

    private fun filterPixels(image: BufferedImage, predicate: (Color) -> Boolean): Boolean {
        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                val pixelColor = Color(image.getRGB(x, y), true)
                if (!predicate(pixelColor)) {
                    return false
                }
            }
        }
        return true
    }

    private fun isFullyOpaque(image: BufferedImage): Boolean {
        return filterPixels(image) { color: Color -> color.alpha == 255 }
    }

    private fun isFullyTransparent(image: BufferedImage): Boolean {
        return filterPixels(image) { color: Color -> color.alpha == 0 }
    }

    private fun writeFrame() {
        pendingWrite?.let { writeFrame(gif, it, pendingDuration, pendingDisposeMethod) }
            ?: throw IllegalStateException("No frame to write")
    }

    /**
     * Write a frame to the GIF.
     *
     * @param gif           The GIF to write to.
     * @param image         The image to write.
     * @param duration      The duration of the frame in microseconds.
     * @param disposeMethod The dispose method to use.
     */
    private fun writeFrame(
        gif: GifStream,
        image: BufferedImage,
        duration: Double,
        disposeMethod: DisposeMethod
    ) {
        val immutableImage = ImmutableImage.wrapAwt(image)
        val frameDuration = Duration.of(
            max(duration, GIF_MINIMUM_FRAME_DURATION.toDouble()).toLong(), ChronoUnit.MICROS
        )
        gif.writeFrame(immutableImage, frameDuration, disposeMethod)
    }

    override fun close() {
        if (closed) return
        closed = true
        if (pendingWrite != null) {
            writeFrame()
        }
        gif.close()
    }

    private data class Position(val x: Int, val y: Int)

    /**
     * Indicates that the previous frame had a transparent pixel
     * at a position where the current frame has an opaque pixel,
     * which means that the current frame cannot be optimised.
     */
    private class PreviousTransparentException : Exception()

    object Factory : MediaWriterFactory {
        override val supportedFormats: Set<String> = setOf("gif")

        override fun create(
            output: Path,
            outputFormat: String,
            loopCount: Int,
            audioChannels: Int,
            audioSampleRate: Int,
            audioBitrate: Int,
            maxFileSize: Long,
            maxDuration: Double
        ): MediaWriter = ScrimageGifWriter(output, loopCount)
    }
}
