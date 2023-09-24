package io.github.shaksternano.borgar.core.media.writer

import io.github.shaksternano.borgar.core.media.AudioFrame
import io.github.shaksternano.borgar.core.media.ImageFrame
import java.io.Closeable

interface MediaWriter : Closeable {

    val isStatic: Boolean
    val supportsAudio: Boolean

    fun writeImageFrame(frame: ImageFrame)

    fun writeAudioFrame(frame: AudioFrame)
}
