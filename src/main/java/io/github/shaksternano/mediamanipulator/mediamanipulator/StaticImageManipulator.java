package io.github.shaksternano.mediamanipulator.mediamanipulator;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import io.github.shaksternano.mediamanipulator.util.FileUtil;

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
    public File makeGif(File media, boolean fallback) throws IOException {
        File gifFile = FileUtil.getUniqueTempFile(Files.getNameWithoutExtension(media.getName()) + ".gif");
        if (fallback) {
            Files.move(media, gifFile);
        } else {
            ImageIO.write(ImageIO.read(media), "gif", gifFile);
        }

        return gifFile;
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return ImmutableSet.of(
                "png",
                "jpg",
                "jpeg"
        );
    }

    /**
     * Applies the given operation to the image.
     *
     * @param media         The image based file to apply the operation to.
     * @param operation     The operation to apply.
     * @param operationName The name of the operation.
     * @return The resulting file.
     * @throws IOException If an error occurs while applying the operation.
     */
    @Override
    protected File applyOperation(File media, Function<BufferedImage, BufferedImage> operation, String operationName) throws IOException {
        BufferedImage uneditedImage = ImageIO.read(media);

        BufferedImage editedImage = operation.apply(uneditedImage);

        String extension = Files.getFileExtension(media.getName());
        File editedImageFile = FileUtil.getUniqueTempFile(FileUtil.appendName(media, "_" + operationName).getName());

        ImageIO.write(editedImage, extension, editedImageFile);
        uneditedImage.flush();

        return editedImageFile;
    }
}
