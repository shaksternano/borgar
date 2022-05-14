package io.github.shaksternano.mediamanipulator.graphics.drawable;

import java.awt.*;

public class TextDrawable implements Drawable {

    private final String TEXT;

    public TextDrawable(String text) {
        TEXT = text;
    }

    @Override
    public void draw(Graphics2D graphics, int x, int y) {
        graphics.drawString(TEXT, x, y + getHeight(graphics));
    }

    @Override
    public int getWidth(Graphics2D graphicsContext) {
        return graphicsContext.getFontMetrics().stringWidth(TEXT);
    }

    @Override
    public int getHeight(Graphics2D graphicsContext) {
        FontMetrics metrics = graphicsContext.getFontMetrics();
        return metrics.getAscent() + metrics.getDescent();
    }
}
