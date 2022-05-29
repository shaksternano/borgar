package io.github.shaksternano.mediamanipulator.image.imagemedia;

import com.google.common.collect.ImmutableList;
import io.github.shaksternano.mediamanipulator.image.util.Frame;
import io.github.shaksternano.mediamanipulator.util.CollectionUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
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
        return stream().map(frame -> Collections.nCopies(Math.max(frame.getDuration(), 1), frame.getImage()))
                .flatMap(List::stream)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    public List<BufferedImage> toNormalisedImages() {
        return CollectionUtil.keepEveryNthElement(toBufferedImages(), Frame.GIF_MINIMUM_FRAME_DURATION, Image::flush);
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
