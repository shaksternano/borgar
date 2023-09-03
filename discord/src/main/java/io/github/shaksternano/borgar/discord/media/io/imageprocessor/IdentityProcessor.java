package io.github.shaksternano.borgar.discord.media.io.imageprocessor;

import io.github.shaksternano.borgar.core.media.ImageFrame;

import java.awt.image.BufferedImage;

public enum IdentityProcessor implements SingleImageProcessor<Object> {

    INSTANCE;

    @Override
    public BufferedImage transformImage(ImageFrame frame, Object constantData) {
        return frame.getContent();
    }

    @Override
    public Object constantData(BufferedImage image) {
        return true;
    }
}
