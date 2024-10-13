package io.github.shaksternano.borgar.core.media.writer

import io.github.shaksternano.borgar.core.io.SuspendCloseable
import io.github.shaksternano.borgar.core.io.closeAll
import io.github.shaksternano.borgar.core.media.ImageFrame
import io.github.shaksternano.gifcodec.ChannelOutputAsyncExecutor
import io.github.shaksternano.gifcodec.forEach
import io.github.shaksternano.gifcodec.forEachCurrent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.awt.image.BufferedImage
import kotlin.coroutines.EmptyCoroutineContext

class PreProcessingWriter(
    private val writer: MediaWriter,
    maxConcurrency: Int,
    scope: CoroutineScope = CoroutineScope(EmptyCoroutineContext),
    preProcessImage: (BufferedImage) -> BufferedImage,
): MediaWriter by writer {

    private val preProcessExecutor: ChannelOutputAsyncExecutor<ImageFrame, ImageFrame> = ChannelOutputAsyncExecutor(
        maxConcurrency = maxConcurrency,
        scope = scope,
        task = { frame ->
            frame.copy(content = preProcessImage(frame.content))
        },
    )

    override suspend fun writeImageFrame(frame: ImageFrame) = coroutineScope {
        val preProcessJob = launch {
            preProcessExecutor.submit(frame)
        }
        while (preProcessJob.isActive) {
            preProcessExecutor.output.forEachCurrent {
                writer.writeImageFrame(it.getOrThrow())
            }
        }
    }

    override suspend fun close() = coroutineScope {
        val writeJob = launch {
            preProcessExecutor.output.forEach {
                writer.writeImageFrame(it.getOrThrow())
            }
        }
        closeAll(
            SuspendCloseable {
                preProcessExecutor.close()
                writeJob.join()
            },
            writer,
        )
    }
}
