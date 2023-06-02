package io.github.shaksternano.borgar.image.reader;

import com.google.common.collect.ImmutableSet;
import com.sksamuel.scrimage.nio.AnimatedGif;
import com.sksamuel.scrimage.nio.AnimatedGifReader;
import com.sksamuel.scrimage.nio.ImageSource;
import io.github.shaksternano.borgar.image.imagemedia.ImageMedia;
import io.github.shaksternano.borgar.image.util.AwtFrame;
import io.github.shaksternano.borgar.image.util.ImageMediaBuilder;
import io.github.shaksternano.borgar.media.ImageUtil;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class ScrimageAnimatedGifReader implements ImageReader {

    @Override
    public ImageMedia read(InputStream inputStream, @Nullable Integer type) throws IOException {
        AnimatedGif gif = AnimatedGifReader.read(ImageSource.of(inputStream));
        return processGif(gif, type);
    }

    @Override
    public ImageMedia read(File file, @Nullable Integer type) throws IOException {
        AnimatedGif gif = AnimatedGifReader.read(ImageSource.of(file));
        return processGif(gif, type);
    }

    private static ImageMedia processGif(AnimatedGif gif, @Nullable Integer type) throws IOException {
        ImageMediaBuilder builder = new ImageMediaBuilder();
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
