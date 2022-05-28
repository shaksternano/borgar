package io.github.shaksternano.mediamanipulator.image.imagemedia;

import com.google.common.collect.ImmutableList;
import io.github.shaksternano.mediamanipulator.image.util.Frame;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class BaseImageMedia implements ImageMedia {

    @Override
    public boolean isEmpty() {
        return getFrameCount() == 0;
    }

    @Override
    public boolean isAnimated() {
        return getFrameCount() > 1;
    }

    @Override
    public List<BufferedImage> toBufferedImages() {
        ImmutableList.Builder<BufferedImage> builder = new ImmutableList.Builder<>();

        for (Frame frame : this) {
            for (int i = 0; i < Math.max(frame.getDuration(), 1); i++) {
                builder.add(frame.getImage());
            }
        }

        return builder.build();
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
