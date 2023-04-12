package io.github.shaksternano.mediamanipulator.io.mediawriter;

import org.bytedeco.javacv.Frame;

public abstract class NoAudioWriter implements MediaWriter {

    @Override
    public void recordAudioFrame(Frame frame) {
    }
}
