package io.github.shaksternano.mediamanipulator.util;

import com.sksamuel.scrimage.ImmutableImage;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains static methods for compressing media files.
 */
public class MediaCompression {

    public static final int DISCORD_MAX_DISPLAY_WIDTH = 400;
    public static final int DISCORD_MAX_DISPLAY_HEIGHT = 300;

    public static BufferedImage reduceToDisplaySize(BufferedImage image) {
        BufferedImage oldImage = image;

        if (image.getWidth() > MediaCompression.DISCORD_MAX_DISPLAY_WIDTH) {
            image = ImmutableImage.wrapAwt(image).scaleToWidth(MediaCompression.DISCORD_MAX_DISPLAY_WIDTH).awt();
            oldImage.flush();
        }

        if (image.getHeight() > MediaCompression.DISCORD_MAX_DISPLAY_HEIGHT) {
            image = ImmutableImage.wrapAwt(image).scaleToHeight(MediaCompression.DISCORD_MAX_DISPLAY_HEIGHT).awt();
            oldImage.flush();
        }

        return image;
    }

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
