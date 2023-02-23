package io.github.shaksternano.mediamanipulator.io.mediareader;

import io.github.shaksternano.mediamanipulator.image.ImageFrame;
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

public class JavaxImageReader extends BaseMediaReader<ImageFrame> {

    private final ImageFrame image;

    public JavaxImageReader(File input) throws IOException {
        this(ImageIO.read(input));
    }

    public JavaxImageReader(InputStream input) throws IOException {
        this(ImageIO.read(input));
    }

    private JavaxImageReader(BufferedImage image) {
        this.image = new ImageFrame(image, 0, 0);
        frameCount = 1;
        width = image.getWidth();
        height = image.getHeight();
    }

    @Override
    public ImageFrame getFrame(long timestamp) throws IOException {
        return image;
    }

    @Nullable
    @Override
    public ImageFrame getNextFrame() {
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
    public Iterator<ImageFrame> iterator() {
        return new SingletonIterator<>(image);
    }

    public enum Factory implements MediaReaderFactory<ImageFrame> {

        INSTANCE;

        @Override
        public MediaReader<ImageFrame> createReader(File media) throws IOException {
            return new JavaxImageReader(media);
        }

        @Override
        public MediaReader<ImageFrame> createReader(InputStream media) throws IOException {
            return new JavaxImageReader(media);
        }
    }
}
