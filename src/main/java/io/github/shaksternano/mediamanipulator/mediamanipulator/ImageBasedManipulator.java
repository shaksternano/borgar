package io.github.shaksternano.mediamanipulator.mediamanipulator;

import io.github.shaksternano.mediamanipulator.util.Fonts;
import io.github.shaksternano.mediamanipulator.util.ImageUtil;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Function;

/**
 * A manipulator that works with image based media.
 */
public abstract class ImageBasedManipulator implements MediaManipulator {

    @Override
    public File caption(File media, String caption) throws IOException {
        return applyOperation(media, image -> ImageUtil.captionImage(image, caption, Fonts.getCaptionFont()), "captioned");
    }

    @Override
    public File stretch(File media, float widthMultiplier, float heightMultiplier) throws IOException {
        return applyOperation(media, image -> ImageUtil.stretch(image, (int) (image.getWidth() * widthMultiplier), (int) (image.getHeight() * heightMultiplier)), "stretched");
    }

    @Override
    public File pixelate(File media, int pixelationMultiplier) throws IOException {
        return applyOperation(media, image -> ImageUtil.stretch(
                ImageUtil.stretch(
                        image, image.getWidth() / pixelationMultiplier, image.getHeight() / pixelationMultiplier
                ), image.getWidth(), image.getHeight()
        ), "pixelated");
    }

    @Override
    public File overlayMedia(File media, File overlay, int x, int y, boolean expand, @Nullable Color expandColor, @Nullable String overlayName) throws IOException {
        return applyOperation(media, image -> {
            try {
                BufferedImage overlayImage = ImageIO.read(overlay);
                BufferedImage overLaidImage = ImageUtil.overlayImage(image, overlayImage, x, y, expand, expandColor);
                overlayImage.flush();
                return overLaidImage;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }, overlayName == null ? "overlaid" : overlayName);
    }

    /**
     * Applies the given operation to the given image based file.
     *
     * @param media         The image based file to apply the operation to.
     * @param operation     The operation to apply.
     * @param operationName The name of the operation.
     * @return The resulting file.
     * @throws IOException If an error occurs while applying the operation.
     */
    protected abstract File applyOperation(File media, Function<BufferedImage, BufferedImage> operation, String operationName) throws IOException;
}
