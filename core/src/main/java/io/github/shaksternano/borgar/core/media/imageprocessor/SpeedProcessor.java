package io.github.shaksternano.borgar.core.media.imageprocessor;

import io.github.shaksternano.borgar.core.media.ImageFrameOld;

import java.awt.image.BufferedImage;

public record SpeedProcessor(float speed) implements SingleImageProcessor<Boolean> {

    @Override
    public BufferedImage transformImage(ImageFrameOld frame, Boolean constantData) {
        return frame.getContent();
    }

    @Override
    public Boolean constantData(BufferedImage image) {
        return true;
    }

    @Override
    public float speed() {
        return speed;
    }
}
