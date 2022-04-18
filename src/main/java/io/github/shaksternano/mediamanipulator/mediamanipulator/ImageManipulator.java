package io.github.shaksternano.mediamanipulator.mediamanipulator;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import io.github.shaksternano.mediamanipulator.util.FileUtil;
import io.github.shaksternano.mediamanipulator.util.Fonts;
import io.github.shaksternano.mediamanipulator.util.ImageUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
