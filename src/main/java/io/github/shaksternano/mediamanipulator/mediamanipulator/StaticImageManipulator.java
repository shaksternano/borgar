package io.github.shaksternano.mediamanipulator.mediamanipulator;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import io.github.shaksternano.mediamanipulator.util.FileUtil;
import io.github.shaksternano.mediamanipulator.util.MediaCompression;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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

    @Override
    public File reduceFps(File media, int fpsReductionRatio) {
        throw new UnsupportedOperationException("Cannot reduce the FPS of a static image.");
    }

    @Override
    public File makeGif(File media) throws IOException {
        File gifFile = FileUtil.getUniqueTempFile(Files.getNameWithoutExtension(media.getName()) + ".gif");
        Files.move(media, gifFile);
        return gifFile;
    }

    @Override
    public File compress(File media) throws IOException {
        if (media.length() > FileUtil.DISCORD_MAXIMUM_FILE_SIZE) {
            media = applyOperation(media, MediaCompression::reduceToDisplaySize, "resized", false);

            while (media.length() > FileUtil.DISCORD_MAXIMUM_FILE_SIZE) {
                media = resize(media, 0.75F, false, false);
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
    protected File applyOperation(File media, Function<BufferedImage, BufferedImage> operation, String operationName, boolean compressionNeeded) throws IOException {
        BufferedImage uneditedImage = ImageIO.read(media);

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
