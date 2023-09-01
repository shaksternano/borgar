package io.github.shaksternano.borgar.media.io.writer;

import io.github.shaksternano.borgar.media.AudioFrame;

public interface NoAudioWriter extends MediaWriter {

    @Override
    default void writeAudioFrame(AudioFrame frame) {
    }

    @Override
    default boolean supportsAudio() {
        return false;
    }
}
