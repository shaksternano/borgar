package io.github.shaksternano.borgar.core.media.writer

import io.github.shaksternano.borgar.core.media.AudioFrame
import io.github.shaksternano.borgar.core.media.ImageFrame
import java.io.Closeable

interface MediaWriter : Closeable {

    val isStatic: Boolean
    val supportsAudio: Boolean

    suspend fun writeImageFrame(frame: ImageFrame)

    suspend fun writeAudioFrame(frame: AudioFrame)
}
