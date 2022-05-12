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
        return reduceToSize(image, DISCORD_MAX_DISPLAY_WIDTH, DISCORD_MAX_DISPLAY_HEIGHT);
    }

    public static BufferedImage reduceToSize(BufferedImage image, int width, int height) {
        BufferedImage oldImage = image;
        image = ImmutableImage.wrapAwt(image).bound(width, height).awt();
        oldImage.flush();
        return image;
    }

    /**
     * Removes frames from a list of {@link DurationImage}s.
     *
     * @param frames     The list of DurationImages to remove frames from.
     * @param frameRatio The ratio of frames to keep. For example, if frameRatio is 4, then every 4th frame will be kept.
     * @return The list of DurationImage with frames removed.
     */
    public static List<DurationImage> removeFrames(List<DurationImage> frames, int frameRatio) {
        if (frames.size() <= 1) {
            return frames;
        } else {
            List<DurationImage> keptFrames = new ArrayList<>();

            int keptIndex = -1;
            for (int i = 0; i < frames.size(); i++) {
                if (i % frameRatio == 0) {
                    keptFrames.add(frames.get(i));
                    keptIndex++;
                } else {
                    DurationImage keptFrame = keptFrames.get(keptIndex);
                    int removedFrameDuration = frames.get(i).getDuration();
                    keptFrame.incrementDuration(removedFrameDuration);
                }
            }

            return keptFrames;
        }
    }
}
