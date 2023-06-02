package io.github.shaksternano.mediamanipulator.media.io.imageprocessor;

import io.github.shaksternano.mediamanipulator.media.ImageFrame;

import java.awt.image.BufferedImage;
import java.io.IOException;

public enum IdentityProcessor implements SingleImageProcessor<Boolean> {

    INSTANCE;

    @Override
    public BufferedImage transformImage(ImageFrame frame, Boolean constantData) throws IOException {
        return frame.content();
    }

    @Override
    public Boolean constantData(BufferedImage image) throws IOException {
        return true;
    }
}
