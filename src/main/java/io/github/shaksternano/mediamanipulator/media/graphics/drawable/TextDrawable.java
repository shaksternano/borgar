package io.github.shaksternano.mediamanipulator.media.graphics.drawable;

import java.awt.*;
import java.util.Objects;

public class TextDrawable implements Drawable {

    private final String TEXT;

    public TextDrawable(String text) {
        TEXT = text;
    }

    @Override
    public void draw(Graphics2D graphics, int x, int y, long timestamp) {
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
    public Drawable resizeToWidth(int width) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Drawable resizeToHeight(int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getFrameCount() {
        return 1;
    }

    @Override
    public long getDuration() {
        return 0;
    }

    @Override
    public boolean sameAsPreviousFrame() {
        return true;
    }

    @Override
    public int hashCode() {
        return TEXT.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (Objects.equals(getClass(), obj.getClass())) {
            TextDrawable other = (TextDrawable) obj;
            return Objects.equals(TEXT, other.TEXT);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[Text: " + TEXT + "]";
    }

    protected String getText() {
        return TEXT;
    }
}
