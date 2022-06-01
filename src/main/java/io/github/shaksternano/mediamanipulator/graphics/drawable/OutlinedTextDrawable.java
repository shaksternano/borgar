package io.github.shaksternano.mediamanipulator.graphics.drawable;

import io.github.shaksternano.mediamanipulator.image.util.ImageUtil;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.util.Objects;

public class OutlinedTextDrawable implements Drawable {

    private final String TEXT;
    private final Color TEXT_FILL_COLOR;
    private final Color TEXT_OUTLINE_COLOR;
    private final float TEXT_OUTLINE_WIDTH;

    public OutlinedTextDrawable(String text, Color textFillColor, Color textOutlineColor, float textOutlineWidth) {
        TEXT = text;
        TEXT_FILL_COLOR = textFillColor;
        TEXT_OUTLINE_COLOR = textOutlineColor;
        TEXT_OUTLINE_WIDTH = textOutlineWidth;
    }

    @Override
    public void draw(Graphics2D graphics, int x, int y) {
        BasicStroke outlineStroke = new BasicStroke(TEXT_OUTLINE_WIDTH);

        Color originalColor = graphics.getColor();
        Stroke originalStroke = graphics.getStroke();
        RenderingHints originalHints = graphics.getRenderingHints();

        AffineTransform transform = graphics.getTransform();
        double originalX = transform.getTranslateX();
        double originalY = transform.getTranslateY();

        Shape textShape = getTextShape(graphics);

        ImageUtil.configureTextDrawQuality(graphics);

        graphics.setColor(TEXT_OUTLINE_COLOR);
        graphics.setStroke(outlineStroke);
        graphics.translate(x, y);
        graphics.draw(textShape);

        graphics.setColor(TEXT_FILL_COLOR);
        graphics.fill(textShape);

        graphics.setColor(originalColor);
        graphics.setStroke(originalStroke);
        graphics.setRenderingHints(originalHints);
        graphics.translate(originalX, originalY);
    }

    @Override
    public int getWidth(Graphics2D graphicsContext) {
        return (int) (getTextShape(graphicsContext).getBounds2D().getWidth() + TEXT_OUTLINE_WIDTH * 2);
    }

    @Override
    public int getHeight(Graphics2D graphicsContext) {
        return (int) (getTextShape(graphicsContext).getBounds2D().getHeight() + TEXT_OUTLINE_WIDTH * 2);
    }

    private Shape getTextShape(Graphics2D graphicsContext) {
        Font font = graphicsContext.getFont();
        FontRenderContext fontRenderContext = graphicsContext.getFontRenderContext();
        GlyphVector glyphVector = font.createGlyphVector(fontRenderContext, TEXT);
        return glyphVector.getOutline();
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
    public boolean sameAsPreviousFrame() {
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(TEXT, TEXT_FILL_COLOR, TEXT_OUTLINE_COLOR, TEXT_OUTLINE_WIDTH);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof OutlinedTextDrawable other) {
            return Objects.equals(TEXT, other.TEXT) &&
                    Objects.equals(TEXT_FILL_COLOR, other.TEXT_FILL_COLOR) &&
                    Objects.equals(TEXT_OUTLINE_COLOR, other.TEXT_OUTLINE_COLOR) &&
                    Objects.equals(TEXT_OUTLINE_WIDTH, other.TEXT_OUTLINE_WIDTH);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[Text: " + TEXT + ", TextFillColor: " + TEXT_FILL_COLOR + ", TextOutlineColor: " + TEXT_OUTLINE_COLOR + ", TextOutlineWidth: " + TEXT_OUTLINE_WIDTH + "]";
    }
}
