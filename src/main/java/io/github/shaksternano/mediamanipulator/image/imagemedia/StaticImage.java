package io.github.shaksternano.mediamanipulator.image.imagemedia;

import com.google.common.collect.Iterators;
import io.github.shaksternano.mediamanipulator.graphics.drawable.ImageDrawable;
import io.github.shaksternano.mediamanipulator.image.util.AwtFrame;
import io.github.shaksternano.mediamanipulator.image.util.Frame;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;

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
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public int getFrameCount() {
        return 1;
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
        return Objects.hashCode(frame);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof StaticImage other) {
            return Objects.equals(frame, other.frame);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[Frame: " + frame + "]";
    }
}
