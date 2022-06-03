package io.github.shaksternano.mediamanipulator.image.io.writer;

import com.google.common.collect.ImmutableSet;
import com.sksamuel.scrimage.DisposeMethod;
import com.sksamuel.scrimage.ImmutableImage;
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
        StreamingGifWriter writer = new StreamingGifWriter();
        try (StreamingGifWriter.GifStream gif = writer.prepareStream(file, BufferedImage.TYPE_INT_ARGB)) {
            for (Frame frame : image) {
                gif.writeFrame(ImmutableImage.wrapAwt(frame.getImage()), Duration.ofMillis(frame.getDuration()), DisposeMethod.RESTORE_TO_BACKGROUND_COLOR);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public Set<String> getSupportedFormats() {
        return ImmutableSet.of(
                "gif"
        );
    }
}
