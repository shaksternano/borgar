package io.github.shaksternano.mediamanipulator.graphics.drawable;

import io.github.shaksternano.mediamanipulator.image.util.ImageUtil;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.util.Objects;

public class OutlinedTextDrawable extends TextDrawable {

    private final Color TEXT_FILL_COLOR;
    private final Color TEXT_OUTLINE_COLOR;
    private final float TEXT_OUTLINE_WIDTH_RATIO;

    public OutlinedTextDrawable(String text, Color textFillColor, Color textOutlineColor, float textOutlineWidthRatio) {
        super(text);
        TEXT_FILL_COLOR = textFillColor;
        TEXT_OUTLINE_COLOR = textOutlineColor;
        TEXT_OUTLINE_WIDTH_RATIO = textOutlineWidthRatio;
    }

    @Override
    public void draw(Graphics2D graphics, int x, int y, long timestamp) {
        Font font = graphics.getFont();
        float textOutlineWidth = font.getSize2D() * TEXT_OUTLINE_WIDTH_RATIO;
        int actualX = (int) (x + textOutlineWidth);
        int actualY = y + graphics.getFontMetrics().getAscent();
        BasicStroke outlineStroke = new BasicStroke(textOutlineWidth);

        Color originalColor = graphics.getColor();
        Stroke originalStroke = graphics.getStroke();
        RenderingHints originalHints = graphics.getRenderingHints();

        Shape textShape = createTextShape(graphics);

        ImageUtil.configureTextDrawQuality(graphics);

        graphics.setColor(TEXT_OUTLINE_COLOR);
        graphics.setStroke(outlineStroke);
        graphics.translate(actualX, actualY);
        graphics.draw(textShape);

        graphics.setColor(TEXT_FILL_COLOR);
        graphics.fill(textShape);

        graphics.setColor(originalColor);
        graphics.setStroke(originalStroke);
        graphics.setRenderingHints(originalHints);
        graphics.translate(-actualX, -actualY);
    }

    @Override
    public int getWidth(Graphics2D graphicsContext) {
        Font font = graphicsContext.getFont();
        float textOutlineWidth = font.getSize2D() * TEXT_OUTLINE_WIDTH_RATIO;
        return (int) (createTextShape(graphicsContext).getBounds2D().getWidth() + textOutlineWidth * 2);
    }

    @Override
    public int getHeight(Graphics2D graphicsContext) {
        Font font = graphicsContext.getFont();
        float textOutlineWidth = font.getSize2D() * TEXT_OUTLINE_WIDTH_RATIO;
        return (int) (createTextShape(graphicsContext).getBounds2D().getHeight() + textOutlineWidth * 2);
    }

    private Shape createTextShape(Graphics2D graphicsContext) {
        Font font = graphicsContext.getFont();
        FontRenderContext fontRenderContext = graphicsContext.getFontRenderContext();
        GlyphVector glyphVector = font.createGlyphVector(fontRenderContext, getText());
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
        return Objects.hash(getText(), TEXT_FILL_COLOR, TEXT_OUTLINE_COLOR, TEXT_OUTLINE_WIDTH_RATIO);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof OutlinedTextDrawable other) {
            return Objects.equals(getText(), other.getText())
                    && Objects.equals(TEXT_FILL_COLOR, other.TEXT_FILL_COLOR)
                    && Objects.equals(TEXT_OUTLINE_COLOR, other.TEXT_OUTLINE_COLOR)
                    && Objects.equals(TEXT_OUTLINE_WIDTH_RATIO, other.TEXT_OUTLINE_WIDTH_RATIO);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[Text: " + getText() + ", TextFillColor: " + TEXT_FILL_COLOR + ", TextOutlineColor: " + TEXT_OUTLINE_COLOR + ", TextOutlineWidth: " + TEXT_OUTLINE_WIDTH_RATIO + "]";
    }
}
