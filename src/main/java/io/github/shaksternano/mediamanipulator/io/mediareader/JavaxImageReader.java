package io.github.shaksternano.mediamanipulator.io.mediareader;

import io.github.shaksternano.mediamanipulator.io.MediaReaderFactory;
import org.apache.commons.collections4.iterators.SingletonIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class JavaxImageReader extends BaseMediaReader<BufferedImage> {

    private final BufferedImage image;

    public JavaxImageReader(File input) throws IOException {
        this.image = ImageIO.read(input);
    }

    public JavaxImageReader(InputStream input) throws IOException {
        this.image = ImageIO.read(input);
    }

    @Override
    public BufferedImage getFrame(long timestamp) throws IOException {
        return image;
    }

    @Nullable
    @Override
    public BufferedImage getNextFrame() {
        return image;
    }

    @Override
    public long getTimestamp() {
        return 0;
    }

    @Override
    public void setTimestamp(long timestamp) {
    }

    @Override
    public void close() {
    }

    @NotNull
    @Override
    public Iterator<BufferedImage> iterator() {
        return new SingletonIterator<>(image);
    }

    public enum Factory implements MediaReaderFactory<BufferedImage> {

        INSTANCE;

        @Override
        public MediaReader<BufferedImage> createReader(File media) throws IOException {
            return new JavaxImageReader(media);
        }

        @Override
        public MediaReader<BufferedImage> createReader(InputStream media) throws IOException {
            return new JavaxImageReader(media);
        }
    }
}
