package io.github.shaksternano.borgar.core.media.reader

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.SuspendCloseable
import io.github.shaksternano.borgar.core.io.closeAll
import io.github.shaksternano.borgar.core.io.use
import io.github.shaksternano.borgar.core.media.VideoFrame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.seconds

abstract class FFmpegMediaReader<T : VideoFrame<*>>(
    protected val input: Path,
    private val isTempFile: Boolean,
    private val grabber: FFmpegFrameGrabber,
) : BaseMediaReader<T>() {

    final override val loopCount: Int = 0

    override suspend fun readFrame(timestamp: Duration): T = readFrame(timestamp, grabber)

    protected suspend fun readFrame(timestamp: Duration, grabber: FFmpegFrameGrabber): T {
        val circularTimestamp =
            if (timestamp == duration) timestamp
            else (timestamp.inWholeMicroseconds % max(duration.inWholeMicroseconds, 1)).microseconds
        return readFrameNonCircular(circularTimestamp, grabber)
    }

    private suspend fun readFrameNonCircular(timestamp: Duration, grabber: FFmpegFrameGrabber): T {
        val frame = findFrame(timestamp, grabber)
        return convertFrame(frame)
    }

    private suspend fun findFrame(timestamp: Duration, grabber: FFmpegFrameGrabber): Frame {
        setTimestamp(timestamp, grabber)
        val frame = grabFrame(grabber)
            ?: throw NoSuchElementException("No frame at timestamp $timestamp")
        // The next call to grabFrame() will overwrite the current frame object, so we need to clone it
        var correctFrame = frame.clone()
        // The frame grabbed might not have the exact timestamp, even if there is a frame with that timestamp.
        // We keep grabbing frames, until we find one with a timestamp greater or equal to the requested timestamp.
        while (correctFrame.timestamp.microseconds < timestamp) {
            val newFrame = grabFrame(grabber)
            if (newFrame == null || newFrame.timestamp.microseconds > timestamp) {
                return correctFrame
            }
            withContext(Dispatchers.IO) {
                correctFrame.close()
            }
            val copy = newFrame.clone()
            withContext(Dispatchers.IO) {
                newFrame.close()
            }
            correctFrame = copy
        }
        return correctFrame
    }

    protected abstract suspend fun setTimestamp(timestamp: Duration, grabber: FFmpegFrameGrabber)

    protected abstract suspend fun grabFrame(grabber: FFmpegFrameGrabber): Frame?

    private suspend fun grabConvertedFrame(grabber: FFmpegFrameGrabber): T? {
        val frame = grabFrame(grabber) ?: return null
        return convertFrame(frame)
    }

    protected abstract fun convertFrame(frame: Frame): T

    override suspend fun close() = closeAll(
        SuspendCloseable.fromBlocking(grabber),
        SuspendCloseable.fromBlocking {
            if (isTempFile) {
                input.deleteIfExists()
            }
        }
    )

    override fun asFlow(): Flow<T> = flow {
        val grabber = FFmpegFrameGrabber(input.toFile())
        withContext(Dispatchers.IO) {
            grabber.start()
        }
        SuspendCloseable.fromBlocking(grabber).use {
            var nextFrame = grabConvertedFrame(grabber)
            while (nextFrame != null) {
                emit(nextFrame)
                nextFrame = grabConvertedFrame(grabber)
            }
        }
    }
}

suspend fun <T : FFmpegMediaReader<*>> createReader(
    input: DataSource,
    factory: (
        input: Path,
        isTempFile: Boolean,
        grabber: FFmpegFrameGrabber,
        frameCount: Int,
        frameRate: Double,
        frameDuration: Duration,
    ) -> T
): T {
    val isTempFile = input.path == null
    val path = input.getOrWriteFile().path
    val grabber = FFmpegFrameGrabber(path.toFile())
    return withContext(Dispatchers.IO) {
        grabber.start()
        var frameCount = 0
        var frame: Frame?
        while (grabber.grabImage().also { frame = it } != null) {
            frameCount++
            frame?.close()
        }
        val frameRate = grabber.frameRate
        factory(
            path,
            isTempFile,
            grabber,
            frameCount,
            frameRate,
            (1 / frameRate).seconds,
        )
    }
}
