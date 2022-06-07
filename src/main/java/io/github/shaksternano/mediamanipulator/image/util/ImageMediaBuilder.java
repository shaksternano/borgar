package io.github.shaksternano.mediamanipulator.image.util;

import io.github.shaksternano.mediamanipulator.image.imagemedia.AnimatedImage;
import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;
import io.github.shaksternano.mediamanipulator.image.imagemedia.StaticImage;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ImageMediaBuilder {

    private final List<Frame> frames = new ArrayList<>();

    public ImageMediaBuilder add(Frame... frames) {
        for (Frame frame : frames) {
            if (this.frames.isEmpty()) {
                this.frames.add(frame);
            } else {
                int lastIndex = this.frames.size() - 1;
                Frame lastFrame = this.frames.get(lastIndex);
                if (frame.getImage().equals(lastFrame.getImage())) {
                    int newDuration = frame.getDuration() + lastFrame.getDuration();
                    this.frames.set(lastIndex, frame.copyWithDuration(newDuration));
                } else {
                    this.frames.add(frame);
                }
            }
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

    public int getFrameCount() {
        return frames.size();
    }

    public ImageMediaBuilder increaseLastFrameDuration(int duration) {
        if (frames.isEmpty()) {
            throw new IllegalStateException("Builder is empty!");
        } else if (duration < 0) {
            throw new IllegalArgumentException("Duration must be positive or 0!");
        } else {
            int lastIndex = frames.size() - 1;
            Frame lastFrame = frames.get(lastIndex);
            int newDuration = lastFrame.getDuration() + duration;
            frames.set(lastIndex, lastFrame.copyWithDuration(newDuration));
        }

        return this;
    }

    public ImageMedia build() {
        if (frames.size() == 1) {
            return new StaticImage(frames.get(0).getImage());
        } else {
            return new AnimatedImage(frames);
        }
    }

    public static ImageMedia fromCollection(Iterable<Frame> frames) {
        int frameCount = 0;
        BufferedImage firstImage = null;
        for (Frame frame : frames) {
            if (firstImage == null) {
                firstImage = frame.getImage();
            }

            frameCount++;

            if (frameCount > 1) {
                break;
            }
        }

        if (frameCount == 1) {
            return new StaticImage(firstImage);
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
