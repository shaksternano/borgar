package io.github.shaksternano.mediamanipulator.graphics.drawable;

import io.github.shaksternano.mediamanipulator.image.util.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

public class ImageDrawable implements Drawable {

    private final BufferedImage image;

    public ImageDrawable(BufferedImage image) {
        this.image = image;
    }

    @Override
    public void draw(Graphics2D graphics, int x, int y) {
        graphics.drawImage(image, x, y, null);
    }

    @Override
    public int getWidth(Graphics2D graphicsContext) {
        return image.getWidth();
    }

    @Override
    public int getHeight(Graphics2D graphicsContext) {
        return image.getHeight();
    }

    @Override
    public Drawable resizeToWidth(int width) {
        if (image.getWidth() == width) {
            return this;
        } else {
            return new ImageDrawable(ImageUtil.fitWidth(image, width));
        }
    }

    @Override
    public Drawable resizeToHeight(int height) {
        if (image.getHeight() == height) {
            return this;
        } else {
            return new ImageDrawable(ImageUtil.fitHeight(image, height));
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(image);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof ImageDrawable other) {
            return Objects.equals(image, other.image);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + image + "]";
    }
}
