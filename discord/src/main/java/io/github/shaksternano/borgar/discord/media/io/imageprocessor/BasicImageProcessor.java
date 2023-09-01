package io.github.shaksternano.borgar.discord.media.io.imageprocessor;

import io.github.shaksternano.borgar.discord.media.ImageFrame;

import java.awt.image.BufferedImage;
import java.util.function.UnaryOperator;

public record BasicImageProcessor(UnaryOperator<BufferedImage> imageMapper) implements SingleImageProcessor<Object> {

    @Override
    public BufferedImage transformImage(ImageFrame frame, Object constantData) {
        return imageMapper.apply(frame.content());
    }

    @Override
    public Object constantData(BufferedImage image) {
        return true;
    }
}
