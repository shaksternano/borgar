package io.github.shaksternano.borgar.core.media.readerold;

import com.google.common.collect.Iterators;
import io.github.shaksternano.borgar.core.collect.ClosableIteratorOld;
import io.github.shaksternano.borgar.core.collect.ClosableSpliteratorOld;
import io.github.shaksternano.borgar.core.media.ImageFrameOld;
import io.github.shaksternano.borgar.core.media.ImageUtil;
import io.github.shaksternano.borgar.core.media.MediaReaderFactory;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

public class JavaxImageReader extends BaseMediaReader<ImageFrameOld> {

    private final ImageFrameOld image;

    public JavaxImageReader(File input, String format) throws IOException {
        this(ImageIO.read(input), format);
    }

    public JavaxImageReader(InputStream input, String format) throws IOException {
        this(ImageIO.read(input), format);
        input.close();
    }

    private JavaxImageReader(@Nullable BufferedImage image, String format) throws IOException {
        super(format);
        if (image == null) {
            throw new IOException("Failed to read image");
        }
        // For some reason some images have a greyscale type, even though they have color
        var converted = ImageUtil.convertType(image, BufferedImage.TYPE_INT_ARGB);
        this.image = new ImageFrameOld(converted, 1, 0);
        frameCount = 1;
        duration = 1;
        frameRate = 1;
        frameDuration = 1;
        width = converted.getWidth();
        height = converted.getHeight();
    }

    @Override
    public ImageFrameOld readFrame(long timestamp) {
        return image;
    }

    @Override
    public ImageFrameOld first() {
        return image;
    }

    @Override
    public MediaReader<ImageFrameOld> reversed() {
        return this;
    }

    @Override
    public ClosableIteratorOld<ImageFrameOld> iterator() {
        return ClosableIteratorOld.wrap(Iterators.singletonIterator(image));
    }

    @Override
    public void forEach(Consumer<? super ImageFrameOld> action) {
        action.accept(image);
    }

    @Override
    public ClosableSpliteratorOld<ImageFrameOld> spliterator() {
        return ClosableSpliteratorOld.wrap(List.of(image).spliterator());
    }

    @Override
    public void close() {
    }

    public enum Factory implements MediaReaderFactory<ImageFrameOld> {

        INSTANCE;

        @Override
        public MediaReader<ImageFrameOld> createReader(File media, String format) throws IOException {
            return new JavaxImageReader(media, format);
        }

        @Override
        public MediaReader<ImageFrameOld> createReader(InputStream media, String format) throws IOException {
            return new JavaxImageReader(media, format);
        }
    }
}
