package io.github.shaksternano.borgar.discord.media.graphics.drawable;

import io.github.shaksternano.borgar.discord.media.ImageFrame;
import io.github.shaksternano.borgar.discord.media.ImageUtil;
import io.github.shaksternano.borgar.discord.media.io.MediaReaders;
import io.github.shaksternano.borgar.discord.media.io.reader.MediaReader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class ImageDrawable implements Drawable {

    private final MediaReader<ImageFrame> reader;
    private final BufferedImage firstFrame;
    private int targetWidth;
    private int targetHeight;
    private int actualWidth;
    private int actualHeight;

    public ImageDrawable(InputStream inputStream, String format) throws IOException {
        reader = MediaReaders.createImageReader(inputStream, format);
        targetWidth = reader.width();
        targetHeight = reader.height();
        firstFrame = reader.first().content();
        actualWidth = firstFrame.getWidth();
        actualHeight = firstFrame.getHeight();
    }

    @Override
    public void draw(Graphics2D graphics, int x, int y, long timestamp) throws IOException {
        var image = resizeImage(reader.readFrame(timestamp).content());
        graphics.drawImage(image, x, y, null);
    }

    private BufferedImage resizeImage(BufferedImage image) {
        image = ImageUtil.fitWidth(image, targetWidth);
        return ImageUtil.fitHeight(image, targetHeight);
    }

    @Override
    public int getWidth(Graphics2D graphicsContext) {
        return actualWidth;
    }

    @Override
    public int getHeight(Graphics2D graphicsContext) {
        return actualHeight;
    }

    @Override
    public Drawable resizeToWidth(int width) {
        if (width != actualWidth) {
            this.targetWidth = width;
            var resized = resizeImage(firstFrame);
            actualWidth = resized.getWidth();
            actualHeight = resized.getHeight();
        }
        return this;
    }

    @Override
    public Drawable resizeToHeight(int height) {
        if (height != actualHeight) {
            this.targetHeight = height;
            var resized = resizeImage(firstFrame);
            actualWidth = resized.getWidth();
            actualHeight = resized.getHeight();
        }
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
        return Objects.hash(
            reader,
            targetWidth,
            targetHeight,
            actualWidth,
            actualHeight
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof ImageDrawable other) {
            return Objects.equals(reader, other.reader)
                && targetWidth == other.targetWidth
                && targetHeight == other.targetHeight
                && actualWidth == other.actualWidth
                && actualHeight == other.actualHeight;
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
