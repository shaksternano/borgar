package io.github.shaksternano.mediamanipulator.mediamanipulator;

import com.google.common.io.Files;
import io.github.shaksternano.mediamanipulator.command.util.InvalidMediaException;
import io.github.shaksternano.mediamanipulator.io.FileUtil;
import io.github.shaksternano.mediamanipulator.util.DurationImage;
import io.github.shaksternano.mediamanipulator.util.Fonts;
import io.github.shaksternano.mediamanipulator.util.ImageUtil;
import io.github.shaksternano.mediamanipulator.util.MediaCompression;
import net.ifok.image.image4j.codec.ico.ICOEncoder;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.function.Function;

/**
 * A manipulator that works with image based media.
 */
public abstract class ImageBasedManipulator implements MediaManipulator {

    @Override
    public File caption(File media, String[] words, Map<String, BufferedImage> images) throws IOException {
        return applyToEachFrame(media, image -> ImageUtil.captionImage(image, words, Fonts.getCaptionFont(), images), "captioned", true);
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
                    speechBubbled = ImageUtil.cutoutImage(image, resizedSpeechBubble, 0, 0, 0xFFFFFF);
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
            } catch (RuntimeException e) {
                String message = e.getMessage();
                if (message != null && message.contains("Error doing rescale. Target size was")) {
                    throw new InvalidMediaException("Image dimensions " + image.getWidth() + " x " + image.getHeight() + " is too small!");
                } else {
                    throw e;
                }
            }
        }, "speech_bubbled", true);
    }

    @Override
    public File rotate(File media, float degrees, @Nullable Color backgroundColor) throws IOException {
        return applyToEachFrame(media, image -> ImageUtil.rotate(image, degrees, null, null, backgroundColor), "rotated", true);
    }

    @Override
    public File makePngOrTransparent(File media) throws IOException {
        File pngFile = FileUtil.getUniqueTempFile(Files.getNameWithoutExtension(media.getName()) + ".png");
        BufferedImage image = ImageUtil.readImageWithAlpha(media);
        ImageIO.write(image, "png", pngFile);
        image.flush();
        return pngFile;
    }

    @Override
    public File makeIco(File media) throws IOException {
        File icoFile = FileUtil.getUniqueTempFile(Files.getNameWithoutExtension(media.getName()) + ".ico");
        BufferedImage image = ImageUtil.readImage(media);
        BufferedImage resizedImage = MediaCompression.reduceToSize(image, 256, 256);
        ICOEncoder.write(resizedImage, icoFile);
        resizedImage.flush();
        return icoFile;
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected File spinFrames(Map<Integer, DurationImage> indexedFrames, float speed, int framesPerRotation, int maxDimension, File originalMedia, @Nullable Color backgroundColor) throws IOException {
        indexedFrames.entrySet().parallelStream().forEach(durationImageEntry -> {
            float index = durationImageEntry.getKey();
            DurationImage frame = durationImageEntry.getValue();
            BufferedImage originalFrame = frame.getImage();
            float angle = 360 * (index / framesPerRotation);

            if (speed < 0) {
                angle = -angle;
            }

            frame.setImage(ImageUtil.rotate(originalFrame, angle, maxDimension, maxDimension, backgroundColor));
            originalFrame.flush();
        });

        File outputFile = FileUtil.getUniqueTempFile(Files.getNameWithoutExtension(originalMedia.getName()) + "_spun.gif");
        originalMedia.delete();

        ImageUtil.writeFramesToGifFile(indexedFrames.values(), outputFile);
        outputFile = compress(outputFile);
        return outputFile;
    }
}
