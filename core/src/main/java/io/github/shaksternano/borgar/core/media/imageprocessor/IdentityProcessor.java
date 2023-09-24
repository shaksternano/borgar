package io.github.shaksternano.borgar.core.media.imageprocessor;

import io.github.shaksternano.borgar.core.media.ImageFrameOld;

import java.awt.image.BufferedImage;

public enum IdentityProcessor implements SingleImageProcessor<Object> {

    INSTANCE;

    @Override
    public BufferedImage transformImage(ImageFrameOld frame, Object constantData) {
        return frame.getContent();
    }

    @Override
    public Object constantData(BufferedImage image) {
        return true;
    }
}
