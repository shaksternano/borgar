package io.github.shaksternano.mediamanipulator.media.io.writer;

import io.github.shaksternano.mediamanipulator.media.AudioFrame;

public abstract class NoAudioWriter implements MediaWriter {

    @Override
    public void recordAudioFrame(AudioFrame frame) {
    }
}
