package io.github.shaksternano.borgar.core.media.writer

import io.github.shaksternano.borgar.core.io.SuspendCloseable
import io.github.shaksternano.borgar.core.media.AudioFrame
import io.github.shaksternano.borgar.core.media.ImageFrame

interface MediaWriter : SuspendCloseable {

    val isStatic: Boolean
    val supportsAudio: Boolean

    suspend fun writeImageFrame(frame: ImageFrame)

    suspend fun writeAudioFrame(frame: AudioFrame)
}
