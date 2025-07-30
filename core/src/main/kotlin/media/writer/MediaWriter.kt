package com.shakster.borgar.core.media.writer

import com.shakster.borgar.core.io.SuspendCloseable
import com.shakster.borgar.core.media.AudioFrame
import com.shakster.borgar.core.media.ImageFrame

interface MediaWriter : SuspendCloseable {

    val isStatic: Boolean
    val supportsAudio: Boolean

    suspend fun writeImageFrame(frame: ImageFrame)

    suspend fun writeAudioFrame(frame: AudioFrame)
}
