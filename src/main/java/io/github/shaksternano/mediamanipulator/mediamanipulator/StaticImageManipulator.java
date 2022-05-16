package io.github.shaksternano.mediamanipulator.mediamanipulator;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import io.github.shaksternano.mediamanipulator.command.util.exception.UnsupportedFileTypeException;
import io.github.shaksternano.mediamanipulator.io.FileUtil;
import io.github.shaksternano.mediamanipulator.util.DurationImage;
import io.github.shaksternano.mediamanipulator.util.ImageUtil;
import io.github.shaksternano.mediamanipulator.util.MediaCompression;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
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
        throw new UnsupportedFileTypeException("Cannot change the speed of a static image.");
    }

    @Override
    public File spin(File media, float speed, @Nullable Color backgroundColor) throws IOException {
        BufferedImage image = ImageUtil.readImageWithAlpha(media);
        image = MediaCompression.reduceToDisplaySize(image);

        int maxDimension = Math.max(image.getWidth(), image.getHeight());
        float absoluteSpeed = Math.abs(speed);

        int framesPerRotation = 150;
        if (absoluteSpeed >= 1) {
            framesPerRotation = Math.max((int) (framesPerRotation / absoluteSpeed), 1);
        }

        Map<Integer, DurationImage> indexedFrames = new LinkedHashMap<>(framesPerRotation);
        for (int i = 0; i < framesPerRotation; i++) {
            int duration = DurationImage.GIF_MINIMUM_FRAME_DURATION;
            if (absoluteSpeed < 1) {
                duration /= absoluteSpeed;
            }

            indexedFrames.put(i, new DurationImage(image, duration));
        }

        return spinFrames(indexedFrames, speed, framesPerRotation, maxDimension, media, backgroundColor);
    }

    @Override
    public File reduceFps(File media, int fpsReductionRatio) {
        throw new UnsupportedFileTypeException("Cannot reduce the FPS of a static image.");
    }

    @Override
    public File makeGif(File media, boolean justRenameFile) throws IOException {
        File gifFile = FileUtil.getUniqueTempFile(Files.getNameWithoutExtension(media.getName()) + ".gif");

        if (justRenameFile) {
            Files.move(media, gifFile);
        } else {
            BufferedImage nonGifImage = ImageUtil.readImageWithAlpha(media);
            ImageIO.write(nonGifImage, "gif", gifFile);
            nonGifImage.flush();
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
                "webp",
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
        BufferedImage uneditedImage = ImageUtil.readImage(media);

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
