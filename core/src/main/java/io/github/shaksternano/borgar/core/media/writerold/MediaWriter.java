package io.github.shaksternano.borgar.core.media.writerold;

import io.github.shaksternano.borgar.core.media.AudioFrameOld;
import io.github.shaksternano.borgar.core.media.ImageFrameOld;

import java.io.Closeable;
import java.io.IOException;

public interface MediaWriter extends Closeable {

    void writeImageFrame(ImageFrameOld frame) throws IOException;

    void writeAudioFrame(AudioFrameOld frame) throws IOException;

    boolean isStatic();

    boolean supportsAudio();
}
