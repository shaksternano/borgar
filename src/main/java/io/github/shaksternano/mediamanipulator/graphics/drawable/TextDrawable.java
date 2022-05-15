package io.github.shaksternano.mediamanipulator.graphics.drawable;

import java.awt.*;
import java.util.Objects;

public class TextDrawable implements Drawable {

    private final String TEXT;

    public TextDrawable(String text) {
        TEXT = text;
    }

    @Override
    public void draw(Graphics2D graphics, int x, int y) {
        graphics.drawString(TEXT, x, y + graphics.getFontMetrics().getAscent());
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

    @Override
    public void resizeToWidth(int width) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resizeToHeight(int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof TextDrawable other) {
            return Objects.equals(TEXT, other.TEXT);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return TEXT.hashCode();
    }

    @Override
    public String toString() {
        return "TextDrawable[" + TEXT + "]";
    }
}
