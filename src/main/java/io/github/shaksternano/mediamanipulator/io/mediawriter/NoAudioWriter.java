package io.github.shaksternano.mediamanipulator.io.mediawriter;

import io.github.shaksternano.mediamanipulator.image.AudioFrame;

public abstract class NoAudioWriter implements MediaWriter {

    @Override
    public void recordAudioFrame(AudioFrame frame) {
    }
}
