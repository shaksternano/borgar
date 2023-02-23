package io.github.shaksternano.mediamanipulator.io.mediawriter;

import io.github.shaksternano.mediamanipulator.image.ImageFrame;
import org.bytedeco.javacv.Frame;

import java.io.Closeable;
import java.io.IOException;

public interface MediaWriter extends Closeable {

    void recordImageFrame(ImageFrame frame) throws IOException;

    void recordAudioFrame(Frame frame) throws IOException;
}
