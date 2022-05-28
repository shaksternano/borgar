package io.github.shaksternano.mediamanipulator.image.io.reader;

import com.google.common.collect.ImmutableSet;
import com.sksamuel.scrimage.ImmutableImage;
import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;
import io.github.shaksternano.mediamanipulator.image.imagemedia.StaticImage;
import io.github.shaksternano.mediamanipulator.image.util.ImageUtil;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class ScrimageImageReader implements ImageReader {

    @Override
    public ImageMedia read(InputStream inputStream, @Nullable Integer type) throws IOException {
        BufferedImage image = ImmutableImage.loader().fromStream(inputStream).awt();

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
