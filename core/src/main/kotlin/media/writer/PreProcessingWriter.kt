package com.shakster.borgar.core.media.writer

import com.shakster.borgar.core.io.closeAll
import com.shakster.borgar.core.media.ImageFrame
import com.shakster.borgar.core.util.AsyncExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.io.IOException
import java.awt.image.BufferedImage
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.EmptyCoroutineContext

class PreProcessingWriter(
    private val writer: MediaWriter,
    maxConcurrency: Int,
    scope: CoroutineScope = CoroutineScope(EmptyCoroutineContext),
    private val preProcessImage: (BufferedImage) -> BufferedImage,
): MediaWriter by writer {

    @OptIn(ExperimentalAtomicApi::class)
    private val throwableReference: AtomicReference<Throwable?> = AtomicReference(null)

    private val preProcessExecutor: AsyncExecutor<ImageFrame, ImageFrame> = AsyncExecutor(
        maxConcurrency = maxConcurrency,
        scope = scope,
        task = ::processFrame,
        onOutput = {
            writeFrame(it)
        },
    )

    override suspend fun writeImageFrame(frame: ImageFrame) {
        @OptIn(ExperimentalAtomicApi::class)
        val throwable = throwableReference.load()
        if (throwable != null) {
            throw createException(throwable)
        }
        preProcessExecutor.submit(frame)
    }

    private fun processFrame(frame: ImageFrame): ImageFrame {
        return frame.copy(content = preProcessImage(frame.content))
    }

    private suspend fun writeFrame(frameResult: Result<ImageFrame>) {
        val error = frameResult.exceptionOrNull()
        if (error != null) {
            @OptIn(ExperimentalAtomicApi::class)
            throwableReference.compareAndSet(null, error)
            return
        }
        val output = frameResult.getOrThrow()
        writer.writeImageFrame(output)
    }

    private fun createException(cause: Throwable): IOException {
        return IOException("Error pre-processing frame", cause)
    }

    override suspend fun close() {
        var closeThrowable: Throwable? = null
        try {
            closeAll(
                {
                    preProcessExecutor.close()
                },
                writer,
            )
        } catch (t: Throwable) {
            closeThrowable = t
            throw t
        } finally {
            @OptIn(ExperimentalAtomicApi::class)
            val throwable = throwableReference.load()
            if (throwable != null) {
                val exception = createException(throwable)
                if (closeThrowable == null) {
                    throw exception
                } else {
                    closeThrowable.addSuppressed(exception)
                }
            }
        }
    }
}
