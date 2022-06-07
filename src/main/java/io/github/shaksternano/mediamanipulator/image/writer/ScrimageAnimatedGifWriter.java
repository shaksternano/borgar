package io.github.shaksternano.mediamanipulator.image.writer;

import com.google.common.collect.ImmutableSet;
import com.sksamuel.scrimage.DisposeMethod;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.nio.GifWriter;
import com.sksamuel.scrimage.nio.StreamingGifWriter;
import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;
import io.github.shaksternano.mediamanipulator.image.util.Frame;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Set;

public class ScrimageAnimatedGifWriter implements ImageWriter {

    @Override
    public void write(ImageMedia image, File file, String format) throws IOException {
        if (image.isAnimated()) {
            StreamingGifWriter writer = new StreamingGifWriter();
            try (StreamingGifWriter.GifStream gif = writer.prepareStream(file, BufferedImage.TYPE_INT_ARGB)) {
                for (Frame frame : image) {
                    ImmutableImage immutableImage = ImmutableImage.wrapAwt(frame.getImage());
                    gif.writeFrame(immutableImage, Duration.ofMillis(frame.getDuration()), DisposeMethod.RESTORE_TO_BACKGROUND_COLOR);
                }
            } catch (Exception e) {
                throw new IOException(e);
            }
        } else {
            ImmutableImage immutableImage = ImmutableImage.wrapAwt(image.getFrame(0).getImage());
            immutableImage.output(GifWriter.Default, file);
        }
    }

    @Override
    public Set<String> getSupportedFormats() {
        return ImmutableSet.of(
                "gif"
        );
    }
}
