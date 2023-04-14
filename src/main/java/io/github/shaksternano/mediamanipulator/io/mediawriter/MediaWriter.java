package io.github.shaksternano.mediamanipulator.io.mediawriter;

import io.github.shaksternano.mediamanipulator.image.AudioFrame;
import io.github.shaksternano.mediamanipulator.image.ImageFrame;

import java.io.Closeable;
import java.io.IOException;

public interface MediaWriter extends Closeable {

    void recordImageFrame(ImageFrame frame) throws IOException;

    void recordAudioFrame(AudioFrame frame) throws IOException;
}
