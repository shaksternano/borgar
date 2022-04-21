package io.github.shaksternano.mediamanipulator.util;

import com.google.common.collect.Streams;

import java.awt.image.BufferedImage;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contains static methods for compressing media files.
 */
public class MediaCompression {

    /**
     * Removes frames from a list of {@link DelayedImage}s.
     *
     * @param frames     The list of DelayedImages to remove frames from.
     * @param fileSize   File size of the media file.
     * @param targetSize The target file size to compress to.
     * @return The list of DelayedImages with frames removed.
     */
    public static List<DelayedImage> removeFrames(List<DelayedImage> frames, long fileSize, long targetSize) {
        if (fileSize > targetSize) {
            float frameRatio = ((float) fileSize / targetSize);
            frameRatio *= 6;
            return removeFrames(frames, (int) frameRatio);
        } else {
            return frames;
        }
    }

    /**
     * Removes frames from a list of {@link DelayedImage}s.
     *
     * @param frames     The list of DelayedImages to remove frames from.
     * @param frameRatio The ratio of frames to keep. For example, if frameRatio is 4, then every 4th frame will be kept.
     * @return The list of DelayedImages with frames removed.
     */
    public static List<DelayedImage> removeFrames(List<DelayedImage> frames, int frameRatio) {
        if (frames.size() <= 1) {
            return frames;
        } else {
            int totalFramesTime = 0;
            for (DelayedImage frame : frames) {
                totalFramesTime += frame.getDelay();
            }

            List<BufferedImage> allFrames = new ArrayList<>();

            for (DelayedImage frame : frames) {
                for (int i = 0; i < frame.getDelay(); i++) {
                    allFrames.add(frame.getImage());
                }
            }

            allFrames = Streams
                    .mapWithIndex(allFrames.stream(), AbstractMap.SimpleImmutableEntry::new)
                    .filter(entry -> entry.getValue() % frameRatio == 0)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            List<DelayedImage> keptFrames = new ArrayList<>();

            int remainingDelay = 0;
            for (BufferedImage frame : allFrames) {
                int delayToAdd = frameRatio;
                remainingDelay += delayToAdd;

                if (remainingDelay > totalFramesTime) {
                    delayToAdd -= remainingDelay - totalFramesTime;
                }

                if (keptFrames.isEmpty()) {
                    keptFrames.add(new DelayedImage(frame, delayToAdd));
                } else {
                    DelayedImage delayedImage = keptFrames.get(keptFrames.size() - 1);
                    BufferedImage lastFrame = delayedImage.getImage();

                    if (frame.equals(lastFrame)) {
                        delayedImage.setDelay(delayedImage.getDelay() + delayToAdd);
                    } else {
                        keptFrames.add(new DelayedImage(frame, delayToAdd));
                    }
                }
            }

            return keptFrames;
        }
    }
}
