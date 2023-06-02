package io.github.shaksternano.mediamanipulator.media.io.imageprocessor;

import io.github.shaksternano.mediamanipulator.media.ImageFrame;

import java.awt.image.BufferedImage;
import java.io.IOException;

public enum IdentityProcessor implements SingleImageProcessor<Object> {

    INSTANCE;

    @Override
    public BufferedImage transformImage(ImageFrame frame, Object constantData) throws IOException {
        return frame.content();
    }

    @Override
    public Object constantData(BufferedImage image) throws IOException {
        return true;
    }
}
