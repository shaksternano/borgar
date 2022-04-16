package io.github.shaksternano.mediamanipulator.mediamanipulation;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import io.github.shaksternano.mediamanipulator.util.FileUtil;
import io.github.shaksternano.mediamanipulator.util.Fonts;
import io.github.shaksternano.mediamanipulator.util.ImageUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public enum ImageManipulator implements MediaManipulator {

    INSTANCE;

    @Override
    public File caption(File media, String caption) throws IOException {
        BufferedImage image = ImageIO.read(media);

        BufferedImage captionedImage = ImageUtil.captionImage(image, caption, Fonts.getCaptionFont());

        String extension = Files.getFileExtension(media.getName());
        File captionedImageFile = FileUtil.getUniqueTempFile(FileUtil.appendName(media, "_captioned").getName());

        ImageIO.write(captionedImage, extension, captionedImageFile);

        return captionedImageFile;
    }

    @Override
    public ImmutableSet<String> getSupportedExtensions() {
        return ImmutableSet.of(
                "png",
                "jpg",
                "jpeg"
        );
    }
}
