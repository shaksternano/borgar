package io.github.shaksternano.borgar.media.io.writer;

import io.github.shaksternano.borgar.media.AudioFrame;
import io.github.shaksternano.borgar.media.ImageFrame;

import java.io.Closeable;
import java.io.IOException;

public interface MediaWriter extends Closeable {

    void writeImageFrame(ImageFrame frame) throws IOException;

    void writeAudioFrame(AudioFrame frame) throws IOException;

    boolean isStatic();

    boolean supportsAudio();
}
