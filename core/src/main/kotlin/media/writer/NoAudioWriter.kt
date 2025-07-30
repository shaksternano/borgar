package com.shakster.borgar.core.media.writer

import com.shakster.borgar.core.media.AudioFrame

abstract class NoAudioWriter : MediaWriter {

    final override val supportsAudio: Boolean = false

    final override suspend fun writeAudioFrame(frame: AudioFrame) = Unit
}
