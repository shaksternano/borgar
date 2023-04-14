package io.github.shaksternano.mediamanipulator.graphics.drawable;

import io.github.shaksternano.mediamanipulator.image.ImageFrame;
import io.github.shaksternano.mediamanipulator.image.ImageUtil;
import io.github.shaksternano.mediamanipulator.io.MediaReaders;
import io.github.shaksternano.mediamanipulator.io.mediareader.MediaReader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class ImageDrawable implements Drawable {

    private final MediaReader<ImageFrame> reader;
    private int targetWidth;
    private int targetHeight;
    private BufferedImage firstFrame;

    public ImageDrawable(InputStream inputStream, String format) throws IOException {
        reader = MediaReaders.createImageReader(inputStream, format);
        targetWidth = reader.width();
        targetHeight = reader.height();
        firstFrame = reader.frame(0).content();
    }

    @Override
    public void draw(Graphics2D graphics, int x, int y, long timestamp) throws IOException {
        BufferedImage image = resizeImage(reader.frame(timestamp).content());
        graphics.drawImage(image, x, y, null);
    }

    private BufferedImage resizeImage(BufferedImage image) {
        image = ImageUtil.fitWidth(image, targetWidth);
        return ImageUtil.fitHeight(image, targetHeight);
    }

    @Override
    public int getWidth(Graphics2D graphicsContext) {
        return firstFrame.getWidth();
    }

    @Override
    public int getHeight(Graphics2D graphicsContext) {
        return firstFrame.getHeight();
    }

    @Override
    public Drawable resizeToWidth(int width) {
        this.targetWidth = width;
        firstFrame = resizeImage(firstFrame);
        return this;
    }

    @Override
    public Drawable resizeToHeight(int height) {
        this.targetHeight = height;
        firstFrame = resizeImage(firstFrame);
        return this;
    }

    @Override
    public int getFrameCount() {
        return reader.frameCount();
    }

    @Override
    public long getDuration() {
        return reader.duration();
    }

    @Override
    public boolean sameAsPreviousFrame() {
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reader, targetWidth, targetHeight);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof ImageDrawable other) {
            return Objects.equals(reader, other.reader)
                && targetWidth == other.targetWidth
                && targetHeight == other.targetHeight;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[reader=" + reader + ", width=" + targetWidth + ", height=" + targetHeight + "]";
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
