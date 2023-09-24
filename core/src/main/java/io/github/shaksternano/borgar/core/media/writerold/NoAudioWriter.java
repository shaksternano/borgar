package io.github.shaksternano.borgar.core.media.writerold;

import io.github.shaksternano.borgar.core.media.AudioFrameOld;

public interface NoAudioWriter extends MediaWriter {

    @Override
    default void writeAudioFrame(AudioFrameOld frame) {
    }

    @Override
    default boolean supportsAudio() {
        return false;
    }
}
