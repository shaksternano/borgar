package io.github.shaksternano.borgar.discord.media.io.imageprocessor;

import io.github.shaksternano.borgar.discord.media.ImageFrame;

import java.awt.image.BufferedImage;

public record SpeedProcessor(float speed) implements SingleImageProcessor<Boolean> {

    @Override
    public BufferedImage transformImage(ImageFrame frame, Boolean constantData) {
        return frame.content();
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
