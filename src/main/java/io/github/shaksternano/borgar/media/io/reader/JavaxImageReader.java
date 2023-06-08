package io.github.shaksternano.borgar.media.io.reader;

import com.google.common.collect.Iterators;
import io.github.shaksternano.borgar.media.ImageFrame;
import io.github.shaksternano.borgar.media.io.MediaReaderFactory;
import io.github.shaksternano.borgar.util.collection.ClosableIterator;
import io.github.shaksternano.borgar.util.collection.ClosableSpliterator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

public class JavaxImageReader extends BaseMediaReader<ImageFrame> {

    private final ImageFrame image;

    public JavaxImageReader(File input, String format) throws IOException {
        this(ImageIO.read(input), format);
    }

    public JavaxImageReader(InputStream input, String format) throws IOException {
        this(ImageIO.read(input), format);
        input.close();
    }

    private JavaxImageReader(BufferedImage image, String format) {
        super(format);
        this.image = new ImageFrame(image, 1, 0);
        frameCount = 1;
        duration = 1;
        frameRate = 1;
        frameDuration = 1;
        width = image.getWidth();
        height = image.getHeight();
    }

    @Override
    public ImageFrame readFrame(long timestamp) {
        return image;
    }

    @Override
    public ImageFrame first() {
        return image;
    }

    @Override
    public MediaReader<ImageFrame> reversed() {
        return this;
    }

    @Override
    public ClosableIterator<ImageFrame> iterator() {
        return ClosableIterator.wrap(Iterators.singletonIterator(image));
    }

    @Override
    public void forEach(Consumer<? super ImageFrame> action) {
        action.accept(image);
    }

    @Override
    public ClosableSpliterator<ImageFrame> spliterator() {
        return ClosableSpliterator.wrap(List.of(image).spliterator());
    }

    @Override
    public void close() {
    }

    public enum Factory implements MediaReaderFactory<ImageFrame> {

        INSTANCE;

        @Override
        public MediaReader<ImageFrame> createReader(File media, String format) throws IOException {
            return new JavaxImageReader(media, format);
        }

        @Override
        public MediaReader<ImageFrame> createReader(InputStream media, String format) throws IOException {
            return new JavaxImageReader(media, format);
        }
    }
}
