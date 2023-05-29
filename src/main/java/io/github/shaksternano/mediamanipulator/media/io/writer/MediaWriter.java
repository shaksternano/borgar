package io.github.shaksternano.mediamanipulator.media.io.writer;

import io.github.shaksternano.mediamanipulator.media.AudioFrame;
import io.github.shaksternano.mediamanipulator.media.ImageFrame;

import java.io.Closeable;
import java.io.IOException;

public interface MediaWriter extends Closeable {

    void writeImageFrame(ImageFrame frame) throws IOException;

    void writeAudioFrame(AudioFrame frame) throws IOException;
}
