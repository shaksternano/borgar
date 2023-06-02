package io.github.shaksternano.borgar.image.reader;

import com.google.common.collect.ImmutableSet;
import com.sksamuel.scrimage.ImmutableImage;
import io.github.shaksternano.borgar.image.imagemedia.ImageMedia;
import io.github.shaksternano.borgar.image.imagemedia.StaticImage;
import io.github.shaksternano.borgar.media.ImageUtil;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class ScrimageImageReader implements ImageReader {

    @Override
    public ImageMedia read(InputStream inputStream, @Nullable Integer type) throws IOException {
        BufferedImage image = ImmutableImage.loader().fromStream(inputStream).awt();
        return processImage(image, type);
    }

    @Override
    public ImageMedia read(File file, @Nullable Integer type) throws IOException {
        BufferedImage image = ImmutableImage.loader().fromFile(file).awt();
        return processImage(image, type);
    }

    private static ImageMedia processImage(BufferedImage image, @Nullable Integer type) {
        if (type != null) {
            image = ImageUtil.convertType(image, type);
        }

        return new StaticImage(image);
    }

    @Override
    public Set<String> getSupportedFormats() {
        return ImmutableSet.of(
            "jpeg",
            "jpg",
            "png",
            "gif",
            "tif",
            "tiff",
            "webp"
        );
    }
}
