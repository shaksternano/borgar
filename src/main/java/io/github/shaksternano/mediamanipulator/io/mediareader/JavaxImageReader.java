package io.github.shaksternano.mediamanipulator.io.mediareader;

import org.apache.commons.collections4.iterators.SingletonIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class JavaxImageReader extends BaseMediaReader<BufferedImage> {

    private final BufferedImage image;

    public JavaxImageReader(File file) throws IOException {
        this.image = ImageIO.read(file);
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
}
