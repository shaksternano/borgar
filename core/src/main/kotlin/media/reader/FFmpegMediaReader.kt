package com.shakster.borgar.core.media.reader

import com.shakster.borgar.core.collect.forEachNotNull
import com.shakster.borgar.core.io.*
import com.shakster.borgar.core.media.VideoFrame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.seconds

abstract class FFmpegMediaReader<T : VideoFrame<*>>(
    protected val input: Path,
    protected val grabber: FFmpegFrameGrabber,
    private val isTempFile: Boolean,
) : BaseMediaReader<T>() {

    override val loopCount: Int = 0

    private val mutex: Mutex = Mutex()

    override suspend fun readFrame(timestamp: Duration): T {
        val circularTimestamp =
            if (timestamp == duration) timestamp
            else (timestamp.inWholeMicroseconds % duration.inWholeMicroseconds.coerceAtLeast(1)).microseconds
        return readFrameNonCircular(circularTimestamp)
    }

    private suspend fun readFrameNonCircular(timestamp: Duration): T {
        val frame = findFrame(timestamp)
        return convertFrame(frame)
    }

    private suspend fun findFrame(timestamp: Duration): Frame = mutex.withLock {
        setTimestamp(timestamp)
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
            correctFrame.close()
            val copy = newFrame.clone()
            newFrame.close()
            correctFrame = copy
        }
        return correctFrame
    }

    protected abstract suspend fun setTimestamp(timestamp: Duration)

    protected abstract suspend fun grabFrame(grabber: FFmpegFrameGrabber): Frame?

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
        SuspendCloseable.fromBlocking(grabber).use {
            withContext(IO_DISPATCHER) {
                grabber.start()
            }
            forEachNotNull({
                grabConvertedFrame(grabber)
            }) {
                emit(it)
            }
        }
    }

    private suspend fun grabConvertedFrame(grabber: FFmpegFrameGrabber): T? {
        val frame = grabFrame(grabber) ?: return null
        return convertFrame(frame)
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
    withContext(IO_DISPATCHER) {
        grabber.start()
    }
    var frameCount = 0
    forEachNotNull({
        withContext(IO_DISPATCHER) {
            grabber.grabImage()
        }
    }) {
        frameCount++
        it.close()
    }
    val frameRate = withContext(IO_DISPATCHER) {
        grabber.frameRate
    }
    return factory(
        path,
        isTempFile,
        grabber,
        frameCount,
        frameRate,
        (1 / frameRate).seconds,
    )
}
