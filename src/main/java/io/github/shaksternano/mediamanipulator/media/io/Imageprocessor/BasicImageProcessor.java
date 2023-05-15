package io.github.shaksternano.mediamanipulator.media.io.Imageprocessor;

import io.github.shaksternano.mediamanipulator.media.ImageFrame;

import java.awt.image.BufferedImage;
import java.util.function.UnaryOperator;

public record BasicImageProcessor(UnaryOperator<BufferedImage> imageMapper) implements SingleImageProcessor<Boolean> {

    @Override
    public BufferedImage transformImage(ImageFrame frame, Boolean constantData) {
        return imageMapper.apply(frame.content());
    }

    @Override
    public Boolean constantData(BufferedImage image) {
        return true;
    }
}
