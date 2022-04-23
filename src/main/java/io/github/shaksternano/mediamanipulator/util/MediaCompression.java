package io.github.shaksternano.mediamanipulator.util;

import java.util.ArrayList;
import java.util.List;

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
            frameRatio *= 7;
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
            List<DelayedImage> keptFrames = new ArrayList<>();

            int keptIndex = -1;
            for (int i = 0; i < frames.size(); i++) {
                if (i % frameRatio == 0) {
                    keptFrames.add(frames.get(i));
                    keptIndex++;
                } else {
                    DelayedImage keptFrame = keptFrames.get(keptIndex);
                    int removedFrameDelay = frames.get(i).getDelay();
                    keptFrame.incrementDelay(removedFrameDelay);
                }
            }

            return keptFrames;
        }
    }
}
