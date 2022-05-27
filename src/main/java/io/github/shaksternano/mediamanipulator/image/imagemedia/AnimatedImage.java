package io.github.shaksternano.mediamanipulator.image.imagemedia;

import com.google.common.collect.ImmutableList;
import io.github.shaksternano.mediamanipulator.image.util.Frame;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;

public class AnimatedImage extends BaseImageMedia {

    private final List<Frame> frames;

    public AnimatedImage(Iterable<Frame> frames) {
        this.frames = ImmutableList.copyOf(frames);
    }

    @Override
    public Frame getFrame(int index) {
        return frames.get(index);
    }

    @Override
    public int size() {
        return frames.size();
    }

    @Override
    public Iterator<Frame> iterator() {
        return frames.iterator();
    }

    @Override
    public Spliterator<Frame> spliterator() {
        return frames.spliterator();
    }
}
