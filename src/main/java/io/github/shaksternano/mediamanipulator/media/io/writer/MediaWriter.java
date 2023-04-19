package io.github.shaksternano.mediamanipulator.media.io.writer;

import io.github.shaksternano.mediamanipulator.media.AudioFrame;
import io.github.shaksternano.mediamanipulator.media.ImageFrame;

import java.io.Closeable;
import java.io.IOException;

public interface MediaWriter extends Closeable {

    void recordImageFrame(ImageFrame frame) throws IOException;

    void recordAudioFrame(AudioFrame frame) throws IOException;
}
