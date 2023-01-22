package io.github.shaksternano.mediamanipulator.graphics.drawable;

import io.github.shaksternano.mediamanipulator.image.util.ImageUtil;
import io.github.shaksternano.mediamanipulator.io.mediareader.FFmpegImageReader;
import io.github.shaksternano.mediamanipulator.io.mediareader.MediaReader;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class ImageDrawable implements Drawable {

    private final MediaReader<BufferedImage> reader;
    private int width;
    private int height;

    public ImageDrawable(InputStream inputStream) throws IOException {
        reader = new FFmpegImageReader(inputStream);
        width = reader.getWidth();
        height = reader.getHeight();
    }

    @Override
    public void draw(Graphics2D graphics, int x, int y, long timestamp) throws IOException {
        BufferedImage image = resizeImage(reader.getFrame(timestamp));
        graphics.drawImage(image, x, y, null);
    }

    private BufferedImage resizeImage(BufferedImage image) {
        if (width != image.getWidth() && height != image.getHeight()) {
            image = ImageUtil.fit(image, width, height);
        } else {
            if (width != image.getWidth()) {
                image = ImageUtil.fitWidth(image, width);
            }
            if (height != image.getHeight()) {
                image = ImageUtil.fitHeight(image, height);
            }
        }
        return image;
    }

    @Override
    public int getWidth(Graphics2D graphicsContext) {
        return width;
    }

    @Override
    public int getHeight(Graphics2D graphicsContext) {
        return height;
    }

    @Override
    public Drawable resizeToWidth(int width) {
        this.width = width;
        return this;
    }

    @Override
    public Drawable resizeToHeight(int height) {
        this.height = height;
        return this;
    }

    @Override
    public int getFrameCount() {
        return reader.getFrameCount();
    }

    @Override
    public long getDuration() {
        return reader.getDuration();
    }

    @Override
    public boolean sameAsPreviousFrame() {
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reader, width, height);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof ImageDrawable other) {
            return Objects.equals(reader, other.reader)
                && width == other.width
                && height == other.height;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[reader=" + reader + ", width=" + width + ", height=" + height + "]";
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
