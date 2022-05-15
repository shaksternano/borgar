package io.github.shaksternano.mediamanipulator.graphics.drawable;

import java.awt.*;

public class HorizontalCompositeDrawable extends BaseCompositeDrawable {

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
    public void resizeToWidth(int width) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resizeToHeight(int height) {
        for (Drawable part : getParts()) {
            try {
                part.resizeToHeight(height);
            } catch (UnsupportedOperationException ignored) {
            }
        }
    }
}
