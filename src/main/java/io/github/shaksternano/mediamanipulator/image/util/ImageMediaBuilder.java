package io.github.shaksternano.mediamanipulator.image.util;

import io.github.shaksternano.mediamanipulator.image.imagemedia.AnimatedImage;
import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;
import io.github.shaksternano.mediamanipulator.image.imagemedia.StaticImage;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ImageMediaBuilder {

    private final List<Frame> frames = new ArrayList<>();

    public ImageMediaBuilder add(Frame... frames) {
        if (frames.length == 1) {
            this.frames.add(frames[0]);
        } else {
            this.frames.addAll(Arrays.asList(frames));
        }

        return this;
    }

    public Frame getFrame(int index) {
        return frames.get(index);
    }

    public ImageMediaBuilder setFrame(int index, Frame frame) {
        frames.set(index, frame);
        return this;
    }

    public ImageMedia build() {
        if (frames.size() == 1) {
            return new StaticImage(frames.get(0).getImage());
        } else {
            return new AnimatedImage(frames);
        }
    }

    public static ImageMedia fromCollection(Collection<Frame> frames) {
        if (frames.size() == 1) {
            return new StaticImage(frames.iterator().next().getImage());
        } else {
            return new AnimatedImage(frames);
        }
    }

    public static ImageMedia fromBufferedImages(Collection<BufferedImage> images) {
        if (images.size() == 1) {
            return new StaticImage(images.iterator().next());
        } else {
            List<Frame> frames = new ArrayList<>();

            for (BufferedImage image : images) {
                if (frames.isEmpty()) {
                    frames.add(new AwtFrame(image, 1));
                } else {
                    int previousIndex = frames.size() - 1;
                    Frame previousFrame = frames.get(previousIndex);
                    BufferedImage previousImage = previousFrame.getImage();

                    if (image.equals(previousImage)) {
                        int previousDuration = previousFrame.getDuration();
                        frames.set(previousIndex, new AwtFrame(image, previousDuration + 1));
                    } else {
                        frames.add(new AwtFrame(image, 1));
                    }
                }
            }

            return new AnimatedImage(frames);
        }
    }
}
