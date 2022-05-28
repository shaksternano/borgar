package io.github.shaksternano.mediamanipulator.util;

import com.sksamuel.scrimage.ImmutableImage;
import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;
import io.github.shaksternano.mediamanipulator.image.util.AwtFrame;
import io.github.shaksternano.mediamanipulator.image.util.Frame;
import io.github.shaksternano.mediamanipulator.image.util.ImageMediaBuilder;

import java.awt.image.BufferedImage;

/**
 * Contains static methods for compressing media files.
 */
public class MediaCompression {

    public static BufferedImage reduceToSize(BufferedImage image, int width, int height) {
        BufferedImage oldImage = image;
        image = ImmutableImage.wrapAwt(image).bound(width, height).awt();
        oldImage.flush();
        return image;
    }

    public static ImageMedia removeFrames(ImageMedia imageMedia, int frameRatio) {
        if (imageMedia.isAnimated()) {
            ImageMediaBuilder builder = new ImageMediaBuilder();

            int keptIndex = -1;
            for (int i = 0; i < imageMedia.getFrameCount(); i++) {
                if (i % frameRatio == 0) {
                    builder.add(imageMedia.getFrame(i));
                    keptIndex++;
                } else {
                    Frame keptFrame = builder.getFrame(keptIndex);
                    int removedFrameDuration = imageMedia.getFrame(i).getDuration();
                    builder.setFrame(keptIndex, new AwtFrame(keptFrame.getImage(), keptFrame.getDuration() + removedFrameDuration));
                }
            }
            return builder.build();
        } else {
            return imageMedia;
        }
    }
}
