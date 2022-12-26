package io.github.shaksternano.mediamanipulator.io.mediawriter;

import org.bytedeco.javacv.Frame;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;

public interface MediaWriter extends Closeable {

    void recordImageFrame(BufferedImage frame) throws IOException;

    void recordAudioFrame(Frame frame) throws IOException;
}
