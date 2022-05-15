package io.github.shaksternano.mediamanipulator.graphics.drawable;

import io.github.shaksternano.mediamanipulator.util.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

public class ImageDrawable implements Drawable {

    private BufferedImage image;

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
    public void resizeToWidth(int width) {
        if (image.getWidth() != width) {
            BufferedImage oldImage = image;
            image = ImageUtil.fitWidth(image, width);
            oldImage.flush();
        }
    }

    @Override
    public void resizeToHeight(int height) {
        if (image.getHeight() != height) {
            BufferedImage oldImage = image;
            image = ImageUtil.fitHeight(image, height);
            oldImage.flush();
        }
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
    public int hashCode() {
        return Objects.hashCode(image);
    }

    @Override
    public String toString() {
        return "ImageDrawable[" + image + "]";
    }
}
