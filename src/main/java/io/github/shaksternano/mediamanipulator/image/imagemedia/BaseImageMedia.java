package io.github.shaksternano.mediamanipulator.image.imagemedia;

import io.github.shaksternano.mediamanipulator.image.util.Frame;
import io.github.shaksternano.mediamanipulator.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class BaseImageMedia implements ImageMedia {

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean isAnimated() {
        return size() > 1;
    }

    @Override
    public List<BufferedImage> toBufferedImages() {
        return ImageUtil.framesToBufferedImages(this);
    }

    @Override
    public Stream<Frame> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    public Stream<Frame> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }
}
