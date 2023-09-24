package io.github.shaksternano.borgar.core.media.imageprocessor;

import io.github.shaksternano.borgar.core.media.ImageFrameOld;

import java.awt.image.BufferedImage;
import java.util.function.UnaryOperator;

public record BasicImageProcessor(UnaryOperator<BufferedImage> imageMapper) implements SingleImageProcessor<Object> {

    @Override
    public BufferedImage transformImage(ImageFrameOld frame, Object constantData) {
        return imageMapper.apply(frame.getContent());
    }

    @Override
    public Object constantData(BufferedImage image) {
        return true;
    }
}
