package io.github.shaksternano.mediamanipulator.image.imagemedia;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import io.github.shaksternano.mediamanipulator.image.util.AwtFrame;
import io.github.shaksternano.mediamanipulator.image.util.Frame;

import java.awt.image.BufferedImage;
import java.util.*;

public class StaticImage extends BaseImageMedia {

    private final Frame frame;

    public StaticImage(BufferedImage image) {
        frame = new AwtFrame(image, 1);
    }

    @Override
    public Frame getFrame(int index) {
        if (index == 0) {
            return frame;
        } else {
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for a static image.");
        }
    }

    @Override
    public int getFrameCount() {
        return 1;
    }

    @Override
    public List<BufferedImage> toNormalisedImages() {
        return ImmutableList.of(frame.getImage());
    }

    @Override
    public Iterator<Frame> iterator() {
        return Iterators.singletonIterator(frame);
    }

    @Override
    public Spliterator<Frame> spliterator() {
        return Collections.singleton(frame).spliterator();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(frame.getImage());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof StaticImage other) {
            return Objects.equals(frame.getImage(), other.frame.getImage());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[Image: " + frame.getImage() + "]";
    }
}
