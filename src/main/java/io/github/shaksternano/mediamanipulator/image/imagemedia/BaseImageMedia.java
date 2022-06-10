package io.github.shaksternano.mediamanipulator.image.imagemedia;

import io.github.shaksternano.mediamanipulator.image.util.Frame;

import java.awt.image.BufferedImage;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class BaseImageMedia implements ImageMedia {

    @Override
    public BufferedImage getFirstImage() {
        if (isEmpty()) {
            throw new IllegalStateException("ImageMedia is empty!");
        } else {
            return getFrame(0).getImage();
        }
    }

    @Override
    public boolean isEmpty() {
        return getFrameCount() == 0;
    }

    @Override
    public boolean isAnimated() {
        return getFrameCount() > 1;
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
