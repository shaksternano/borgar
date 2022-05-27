package io.github.shaksternano.mediamanipulator.image.imagemedia;

import com.google.common.collect.Iterators;
import io.github.shaksternano.mediamanipulator.image.util.AwtFrame;
import io.github.shaksternano.mediamanipulator.image.util.Frame;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Iterator;
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
    public int size() {
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
}
