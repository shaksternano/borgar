package io.github.shaksternano.mediamanipulator.image.imagemedia;

import io.github.shaksternano.mediamanipulator.image.util.Frame;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Stream;

public interface ImageMedia extends Iterable<Frame> {

    /**
     * Gets the frame at the specified index.
     *
     * @param index The index of the frame to get.
     * @return The frame at the specified index.
     */
    Frame getFrame(int index);

    BufferedImage getFirstImage();

    /**
     * Gets the number of frames in this ImageMedia.
     *
     * @return The number of frames in this ImageMedia.
     */
    int getFrameCount();

    int getDuration();

    boolean isEmpty();

    boolean isAnimated();

    ImageMedia reverse();

    List<BufferedImage> toNormalisedImages();

    Stream<Frame> stream();

    Stream<Frame> parallelStream();
}
