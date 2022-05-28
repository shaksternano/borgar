package io.github.shaksternano.mediamanipulator.graphics.drawable;

import com.google.common.collect.ImmutableList;
import io.github.shaksternano.mediamanipulator.image.util.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ImageDrawable implements Drawable {

    private final List<BufferedImage> images;
    private int imageIndex = 0;

    public ImageDrawable(Iterable<BufferedImage> images) {
        if (images.iterator().hasNext()) {
            this.images = ImmutableList.copyOf(images);
        } else {
            throw new IllegalArgumentException("Image contains no frames!");
        }
    }

    @Override
    public void draw(Graphics2D graphics, int x, int y) {
        graphics.drawImage(images.get(imageIndex), x, y, null);
        imageIndex++;
        if (imageIndex >= images.size()) {
            imageIndex = 0;
        }
    }

    @Override
    public int getWidth(Graphics2D graphicsContext) {
        return images.get(0).getWidth();
    }

    @Override
    public int getHeight(Graphics2D graphicsContext) {
        return images.get(0).getHeight();
    }

    @Override
    public Drawable resizeToWidth(int width) {
        if (getWidth(null) == width) {
            return this;
        } else {
            ImmutableList.Builder<BufferedImage> builder = ImmutableList.builder();

            for (BufferedImage image : images) {
                builder.add(ImageUtil.fitWidth(image, width));
            }

            return new ImageDrawable(builder.build());
        }
    }

    @Override
    public Drawable resizeToHeight(int height) {
        if (getHeight(null) == height) {
            return this;
        } else {
            ImmutableList.Builder<BufferedImage> builder = ImmutableList.builder();

            for (BufferedImage image : images) {
                builder.add(ImageUtil.fitHeight(image, height));
            }

            return new ImageDrawable(builder.build());
        }
    }

    @Override
    public int getFrameCount() {
        return images.size();
    }

    @Override
    public boolean sameAsPreviousFrame() {
        int previousIndex = imageIndex - 1;
        if (previousIndex < 0) {
            previousIndex = images.size() - 1;
        }

        return images.get(imageIndex).equals(images.get(previousIndex));
    }

    @Override
    public int hashCode() {
        return Objects.hash(images, imageIndex);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof ImageDrawable other) {
            return Objects.equals(images, other.images) &&
                    Objects.equals(imageIndex, other.imageIndex);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getSimpleName() + "[Images: [");

        Iterator<BufferedImage> imageIterator = images.iterator();
        while (imageIterator.hasNext()) {
            BufferedImage image = imageIterator.next();
            builder.append(ImageUtil.imageToString(image));
            if (imageIterator.hasNext()) {
                builder.append(", ");
            }
        }

        builder.append("]")
                .append(", ImageIndex: ")
                .append(imageIndex)
                .append("]");
        return builder.toString();
    }
}
