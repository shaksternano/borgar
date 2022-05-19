package io.github.shaksternano.mediamanipulator.graphics.drawable;

import java.awt.*;
import java.util.Objects;

public class HorizontalCompositeDrawable extends ListCompositeDrawable {

    @Override
    public void draw(Graphics2D graphics, int x, int y) {
        for (Drawable part : getParts()) {
            part.draw(graphics, x, y);
            x += part.getWidth(graphics);
        }
    }

    @Override
    public int getWidth(Graphics2D graphicsContext) {
        int totalWidth = 0;
        for (Drawable part : getParts()) {
            totalWidth += part.getWidth(graphicsContext);
        }

        return totalWidth;
    }

    @Override
    public int getHeight(Graphics2D graphicsContext) {
        int maxHeight = 0;
        for (Drawable part : getParts()) {
            maxHeight = Math.max(maxHeight, part.getHeight(graphicsContext));
        }

        return maxHeight;
    }

    @Override
    public Drawable resizeToWidth(int width) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Drawable resizeToHeight(int height) {
        CompositeDrawable resized = new HorizontalCompositeDrawable();
        boolean resizedAny = false;

        for (Drawable part : getParts()) {
            try {
                part = part.resizeToHeight(height);
                resizedAny = true;
            } catch (UnsupportedOperationException ignored) {
            }

            resized.addPart(part);
        }

        if (resizedAny) {
            return resized;
        } else {
            return this;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof HorizontalCompositeDrawable other) {
            return Objects.equals(getParts(), other.getParts());
        } else {
            return false;
        }
    }
}
