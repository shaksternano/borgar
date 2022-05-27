package io.github.shaksternano.mediamanipulator.image.io.reader;

import com.google.common.collect.ImmutableSet;
import com.sksamuel.scrimage.nio.AnimatedGif;
import com.sksamuel.scrimage.nio.AnimatedGifReader;
import com.sksamuel.scrimage.nio.ImageSource;
import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;
import io.github.shaksternano.mediamanipulator.image.util.AwtFrame;
import io.github.shaksternano.mediamanipulator.image.util.ImageMediaBuilder;
import io.github.shaksternano.mediamanipulator.image.util.ImageUtil;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Set;

public class ScrimageAnimatedGifReader implements ImageReader {

    @Override
    public ImageMedia read(File file, @Nullable Integer type) throws IOException {
        ImageMediaBuilder builder = new ImageMediaBuilder();
        AnimatedGif gif = AnimatedGifReader.read(ImageSource.of(file));
        if (gif.getFrameCount() <= 0) {
            throw new IOException("Could not read any frames!");
        } else {
            for (int i = 0; i < gif.getFrameCount(); i++) {
                BufferedImage image = gif.getFrame(i).awt();
                int duration = (int) gif.getDelay(i).toMillis();

                if (type != null) {
                    image = ImageUtil.convertType(image, type);
                }

                builder.add(new AwtFrame(image, duration));
            }

            return builder.build();
        }
    }

    @Override
    public Set<String> getSupportedFormats() {
        return ImmutableSet.of(
                "gif"
        );
    }
}
