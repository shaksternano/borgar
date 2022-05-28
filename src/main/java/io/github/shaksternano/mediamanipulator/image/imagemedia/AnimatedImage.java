package io.github.shaksternano.mediamanipulator.image.imagemedia;

import com.google.common.collect.ImmutableList;
import io.github.shaksternano.mediamanipulator.image.util.Frame;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
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
    public int getFrameCount() {
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

    @Override
    public int hashCode() {
        return Objects.hashCode(frames);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof AnimatedImage other) {
            return Objects.equals(frames, other.frames);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getSimpleName() + "[Frames: ");

        Iterator<Frame> frameIterator = frames.iterator();
        while (frameIterator.hasNext()) {
            Frame frame = frameIterator.next();
            builder.append(frame);
            if (frameIterator.hasNext()) {
                builder.append(", ");
            }
        }

        builder.append("]");
        return builder.toString();
    }
}
