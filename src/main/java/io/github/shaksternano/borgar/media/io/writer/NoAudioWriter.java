package io.github.shaksternano.borgar.media.io.writer;

import io.github.shaksternano.borgar.media.AudioFrame;

public abstract class NoAudioWriter implements MediaWriter {

    @Override
    public void writeAudioFrame(AudioFrame frame) {
    }

    @Override
    public boolean supportsAudio() {
        return false;
    }
}
