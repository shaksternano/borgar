package io.github.shaksternano.borgar.core.media.writer;

import io.github.shaksternano.borgar.core.media.AudioFrame;

public interface NoAudioWriter extends MediaWriter {

    @Override
    default void writeAudioFrame(AudioFrame frame) {
    }

    @Override
    default boolean supportsAudio() {
        return false;
    }
}
