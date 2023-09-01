package io.github.shaksternano.borgar.discord.media.io.writer;

import io.github.shaksternano.borgar.discord.media.AudioFrame;

public interface NoAudioWriter extends MediaWriter {

    @Override
    default void writeAudioFrame(AudioFrame frame) {
    }

    @Override
    default boolean supportsAudio() {
        return false;
    }
}
