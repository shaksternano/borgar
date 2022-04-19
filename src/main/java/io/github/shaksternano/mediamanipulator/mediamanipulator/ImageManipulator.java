package io.github.shaksternano.mediamanipulator.mediamanipulator;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import io.github.shaksternano.mediamanipulator.util.FileUtil;
import io.github.shaksternano.mediamanipulator.util.Fonts;
import io.github.shaksternano.mediamanipulator.util.ImageUtil;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;
import java.util.function.Function;

public class ImageManipulator implements MediaManipulator {

    @Override
    public File caption(File media, String caption) throws IOException {
        return apply(media, image -> ImageUtil.captionImage(image, caption, Fonts.getCaptionFont()), "captioned");
    }

    @Override
    public File stretch(File media, float widthMultiplier, float heightMultiplier) throws IOException {
        return apply(media, image -> ImageUtil.stretch(image, widthMultiplier, heightMultiplier), "stretched");
    }

    @Override
    public File overlayMedia(File media, File overlay, int x, int y, boolean expand, @Nullable Color excessColor, @Nullable String overlayName) throws IOException {
        return apply(media, image -> {
            try {
                BufferedImage overlayImage = ImageIO.read(overlay);
                BufferedImage overLaidImage = ImageUtil.overlayImage(image, overlayImage, x, y, expand, excessColor);
                overlayImage.flush();
                return overLaidImage;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }, overlayName == null ? "overlaid" : overlayName);
    }

    @Override
    public File makeGif(File media) throws IOException {
        File gifFile = FileUtil.getUniqueTempFile(Files.getNameWithoutExtension(media.getName()) + ".gif");
        ImageIO.write(ImageIO.read(media), "gif", gifFile);
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

    private static File apply(File media, Function<BufferedImage, BufferedImage> operation, String operationName) throws IOException {
        BufferedImage uneditedImage = ImageIO.read(media);

        BufferedImage editedImage = operation.apply(uneditedImage);

        String extension = Files.getFileExtension(media.getName());
        File editedImageFile = FileUtil.getUniqueTempFile(FileUtil.appendName(media, "_" + operationName).getName());

        ImageIO.write(editedImage, extension, editedImageFile);
        uneditedImage.flush();

        return editedImageFile;
    }
}
