package io.github.shaksternano.mediamanipulator.mediamanipulator;

import com.google.common.io.Files;
import io.github.shaksternano.mediamanipulator.util.FileUtil;
import io.github.shaksternano.mediamanipulator.util.Fonts;
import io.github.shaksternano.mediamanipulator.util.ImageUtil;

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
        return applyToEachFrame(media, image -> ImageUtil.captionImage(image, caption, Fonts.getCaptionFont()), "captioned", true);
    }

    @Override
    public File stretch(File media, float widthMultiplier, float heightMultiplier, boolean raw) throws IOException {
        return applyToEachFrame(media, image -> ImageUtil.stretch(image, (int) (image.getWidth() * widthMultiplier), (int) (image.getHeight() * heightMultiplier), raw), "stretched", true);
    }

    @Override
    public File resize(File media, float resizeMultiplier, boolean raw) throws IOException {
        return applyToEachFrame(media, image -> ImageUtil.resize(image, resizeMultiplier, raw), "resized", false);
    }

    @Override
    public File pixelate(File media, int pixelationMultiplier) throws IOException {
        return applyToEachFrame(media, image -> ImageUtil.pixelate(image, pixelationMultiplier), "pixelated", true);
    }

    @Override
    public File speechBubble(File media, boolean cutOut) throws IOException {
        return applyToEachFrame(media, image -> {
            String speechBubblePath = cutOut ? "image/overlay/speech_bubble_2_partial.png" : "image/overlay/speech_bubble_1_partial.png";

            try {
                BufferedImage speechBubble = ImageUtil.getImageResource(speechBubblePath);
                BufferedImage resizedSpeechBubble = ImageUtil.fitWidth(speechBubble, image.getWidth());
                speechBubble.flush();

                BufferedImage speechBubbled;
                if (cutOut) {
                    speechBubbled = ImageUtil.cutoutImage(image, resizedSpeechBubble, 0, 0);
                } else {
                    BufferedImage filledSpeechBubble = ImageUtil.fill(resizedSpeechBubble, Color.WHITE);
                    speechBubbled = ImageUtil.overlayImage(image, filledSpeechBubble, 0, -filledSpeechBubble.getHeight(), true, null);
                    filledSpeechBubble.flush();
                }

                resizedSpeechBubble.flush();
                image.flush();
                return speechBubbled;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }, "speech_bubbled", true);
    }

    @Override
    public File rotate(File media, float degrees) throws IOException {
        return applyToEachFrame(media, image -> ImageUtil.rotate(image, degrees, null, null), "rotated", true);
    }

    @Override
    public File makePngOrTransparent(File media) throws IOException {
        String fileType = FileUtil.getFileType(media);
        File newPngFile = FileUtil.getUniqueTempFile(Files.getNameWithoutExtension(media.getName()) + ".png");
        BufferedImage image = ImageIO.read(media);

        if (fileType.equals("png")) {
            if (image.getColorModel().hasAlpha()) {
                throw new UnsupportedOperationException("The file is already a PNG file and already has transparency!");
            }
        }

        BufferedImage imageWithAlpha = ImageUtil.addAlpha(image);
        image.flush();
        ImageIO.write(imageWithAlpha, "png", newPngFile);
        imageWithAlpha.flush();

        return newPngFile;
    }

    /**
     * Applies the given operation to the given image based file. The original file is deleted after the operation.
     *
     * @param media         The image based file to apply the operation to.
     * @param operation     The operation to apply.
     * @param operationName The name of the operation.
     * @return The resulting file.
     * @throws IOException If an error occurs while applying the operation.
     */
    protected abstract File applyToEachFrame(File media, Function<BufferedImage, BufferedImage> operation, String operationName, boolean compressionNeeded) throws IOException;
}
