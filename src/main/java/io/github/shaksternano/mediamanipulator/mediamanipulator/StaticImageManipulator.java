package io.github.shaksternano.mediamanipulator.mediamanipulator;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import io.github.shaksternano.mediamanipulator.util.DelayedImage;
import io.github.shaksternano.mediamanipulator.util.FileUtil;
import io.github.shaksternano.mediamanipulator.util.ImageUtil;
import io.github.shaksternano.mediamanipulator.util.MediaCompression;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * A manipulator for static image files.
 */
public class StaticImageManipulator extends ImageBasedManipulator {

    @Override
    public File speed(File media, float speedMultiplier) {
        throw new UnsupportedOperationException("Cannot change the speed of a static image.");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public File spin(File media, float speed) throws IOException {
        BufferedImage image = ImageIO.read(media);
        image = MediaCompression.reduceToDisplaySize(image);
        image = ImageUtil.addAlpha(image);

        int width = image.getWidth();
        int height = image.getHeight();
        int maxDimension = Math.max(width, height);

        float absoluteSpeed = Math.abs(speed);
        int frameCount = 150;
        if (absoluteSpeed >= 1) {
            frameCount = Math.max((int) (frameCount / absoluteSpeed), 1);
        }

        Map<Integer, DelayedImage> indexedFrames = new LinkedHashMap<>(frameCount);

        for (int i = 0; i < frameCount; i++) {
            int delay = DelayedImage.GIF_MINIMUM_DELAY;
            if (absoluteSpeed < 1) {
                delay /= absoluteSpeed;
            }

            indexedFrames.put(i, new DelayedImage(image, delay));
        }

        final int finalFrameCount = frameCount;
        indexedFrames.entrySet().parallelStream().forEach(bufferedImageEntry -> {
            int index = bufferedImageEntry.getKey();
            DelayedImage frame = bufferedImageEntry.getValue();
            BufferedImage oldFrame = frame.getImage();
            float angle = 360 * ((index + 1F) / finalFrameCount);

            if (speed < 0) {
                angle = -angle;
            }

            frame.setImage(ImageUtil.rotate(oldFrame, angle, maxDimension, maxDimension));
            oldFrame.flush();
        });

        File outputFile = FileUtil.getUniqueTempFile(Files.getNameWithoutExtension(media.getName()) + "_spun.gif");
        media.delete();

        ImageUtil.writeFramesToGifFile(indexedFrames.values(), outputFile);
        outputFile = MediaManipulators.GIF.compress(outputFile);
        return outputFile;
    }

    @Override
    public File reduceFps(File media, int fpsReductionRatio) {
        throw new UnsupportedOperationException("Cannot reduce the FPS of a static image.");
    }

    @Override
    public File makeGif(File media, boolean justRenameFile) throws IOException {
        File gifFile = FileUtil.getUniqueTempFile(Files.getNameWithoutExtension(media.getName()) + ".gif");

        if (justRenameFile) {
            Files.move(media, gifFile);
        } else {
            BufferedImage nonGifImage = ImageIO.read(media);
            BufferedImage nonGifImageWithAlpha = ImageUtil.addAlpha(nonGifImage);
            ImageIO.write(nonGifImageWithAlpha, "gif", gifFile);
            nonGifImage.flush();
            nonGifImageWithAlpha.flush();
        }

        return gifFile;
    }

    @Override
    public File compress(File media) throws IOException {
        if (media.length() > FileUtil.DISCORD_MAXIMUM_FILE_SIZE) {
            media = applyToEachFrame(media, MediaCompression::reduceToDisplaySize, "resized", false);

            while (media.length() > FileUtil.DISCORD_MAXIMUM_FILE_SIZE) {
                media = resize(media, 0.75F, false);
            }
        }

        return media;
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return ImmutableSet.of(
                "png",
                "jpg",
                "jpeg",
                "bmp",
                "wbmp",
                "tif",
                "tiff"
        );
    }

    /**
     * Applies the given operation to the image. The original file is deleted after the operation.
     *
     * @param media         The image based file to apply the operation to.
     * @param operation     The operation to apply.
     * @param operationName The name of the operation.
     * @return The resulting file.
     * @throws IOException If an error occurs while applying the operation.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    protected File applyToEachFrame(File media, Function<BufferedImage, BufferedImage> operation, String operationName, boolean compressionNeeded) throws IOException {
        BufferedImage uneditedImage = ImageIO.read(media);

        if (compressionNeeded) {
            uneditedImage = MediaCompression.reduceToDisplaySize(uneditedImage);
        }

        BufferedImage editedImage = operation.apply(uneditedImage);

        String extension = Files.getFileExtension(media.getName());
        File editedImageFile = FileUtil.getUniqueTempFile(FileUtil.appendName(media, "_" + operationName).getName());

        media.delete();

        ImageIO.write(editedImage, extension, editedImageFile);
        uneditedImage.flush();

        if (compressionNeeded) {
            editedImageFile = compress(editedImageFile);
        }

        return editedImageFile;
    }
}
