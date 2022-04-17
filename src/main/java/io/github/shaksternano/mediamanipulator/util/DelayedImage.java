package io.github.shaksternano.mediamanipulator.util;

import java.awt.image.BufferedImage;

public class DelayedImage {
    private BufferedImage image;
    private int delay;

    public DelayedImage(BufferedImage image, int delay) {
        this.image = image;
        this.delay = delay;
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getDelay() {
        return delay;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }
}
