package io.github.shaksternano.borgar.core.media

import io.github.shaksternano.borgar.core.util.equalsAnyIgnoreCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import java.io.InputStream
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

val MAX_WRITER_CONCURRENCY: Int = Runtime.getRuntime().availableProcessors()

suspend fun mediaFormat(path: Path): String? = mediaFormatImpl(path.toFile())

suspend fun mediaFormat(inputStream: InputStream): String? {
    return inputStream.use {
        mediaFormatImpl(it)
    }
}

private suspend fun mediaFormatImpl(input: Any): String? {
    return withContext(Dispatchers.IO) {
        ImageIO.createImageInputStream(input).use {
            if (it != null) {
                val readers = ImageIO.getImageReaders(it)
                if (readers.hasNext()) {
                    return@use readers.next().formatName.lowercase()
                }
            }
            null
        }
    }
}

fun <E : VideoFrame<*>> frameAtTime(timestamp: Duration, frames: List<E>, duration: Duration): E {
    val circularTimestamp = (timestamp.inWholeMilliseconds % max(duration.inWholeMilliseconds, 1)).milliseconds
    val index = findIndex(circularTimestamp, frames.map(VideoFrame<*>::timestamp))
    return frames[index]
}

/**
 * Finds the index of the frame with the given timestamp.
 * If there is no frame with the given timestamp, the index of the frame
 * with the highest timestamp smaller than the given timestamp is returned.
 *
 * @param timestamp  The timestamp in microseconds.
 * @param timestamps The frame timestamps in microseconds.
 * @return The index of the frame with the given timestamp.
 */
fun findIndex(timestamp: Duration, timestamps: List<Duration>): Int =
    if (timestamps.isEmpty())
        throw IllegalArgumentException("Timestamp list is empty")
    else if (timestamp < Duration.ZERO)
        throw IllegalArgumentException("Timestamp must not be negative")
    else if (timestamp < timestamps[0])
        throw IllegalArgumentException("Timestamp must not be smaller than the first timestamp")
    else if (timestamp == timestamps[0])
        0
    else if (timestamp < timestamps[timestamps.size - 1])
        findIndexBinarySearch(timestamp, timestamps)
    else {
        // If the timestamp is equal to or greater than the last timestamp.
        timestamps.size - 1
    }

private fun findIndexBinarySearch(timestamp: Duration, timestamps: List<Duration>): Int {
    var low = 0
    var high = timestamps.size - 1
    while (low <= high) {
        val mid = low + (high - low) / 2
        if (timestamps[mid] == timestamp || (timestamps[mid] < timestamp && timestamps[mid + 1] > timestamp))
            return mid
        else if (timestamps[mid] < timestamp)
            low = mid + 1
        else
            high = mid - 1
    }
    throw IllegalStateException("This should never be reached. Timestamp: $timestamp, all timestamps: $timestamps")
}

fun BufferedImage.supportedTransparentImageType(format: String): Int =
    if (supportsTransparency(format)) BufferedImage.TYPE_INT_ARGB
    else typeNoCustom

fun supportsTransparency(format: String): Boolean = format.equalsAnyIgnoreCase(
    "bmp",
    "png",
    "gif",
    "tif",
    "tiff",
    "webp",
)

fun equivalentTransparentFormat(format: String): String =
    if (isJpg(format)) {
        "png"
    } else {
        format
    }

private fun isJpg(format: String): Boolean =
    format.equalsAnyIgnoreCase(
        "jpg",
        "jpeg",
    )

fun isStaticOnly(format: String): Boolean =
    format.equalsAnyIgnoreCase(
        "bmp",
        "jpeg",
        "jpg",
        "wbmp",
        "png",
        "tif",
        "tiff",
    )
