package io.github.shaksternano.borgar.discord.media.graphics.drawable;

import io.github.shaksternano.borgar.discord.media.ImageUtil;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

public class OutlinedTextDrawable extends TextDrawable {

    private final Color textFillColor;
    private final Color textOutlineColor;
    private final float textOutlineWidthRatio;

    public OutlinedTextDrawable(String text, Color textFillColor, Color textOutlineColor, float textOutlineWidthRatio) {
        super(text);
        this.textFillColor = textFillColor;
        this.textOutlineColor = textOutlineColor;
        this.textOutlineWidthRatio = textOutlineWidthRatio;
    }

    @Override
    public void draw(Graphics2D graphics, int x, int y, long timestamp) {
        var font = graphics.getFont();
        var textOutlineWidth = font.getSize2D() * textOutlineWidthRatio;
        var actualX = (int) (x + textOutlineWidth);
        var actualY = y + graphics.getFontMetrics().getAscent();
        var outlineStroke = new BasicStroke(textOutlineWidth);

        var originalColor = graphics.getColor();
        var originalStroke = graphics.getStroke();
        var originalHints = graphics.getRenderingHints();

        var textShape = getShape(graphics);

        ImageUtil.configureTextDrawQuality(graphics);

        graphics.setColor(textOutlineColor);
        graphics.setStroke(outlineStroke);
        graphics.translate(actualX, actualY);
        graphics.draw(textShape);

        graphics.setColor(textFillColor);
        graphics.fill(textShape);

        graphics.setColor(originalColor);
        graphics.setStroke(originalStroke);
        graphics.setRenderingHints(originalHints);
        graphics.translate(-actualX, -actualY);
    }

    @Override
    public int getWidth(Graphics2D graphicsContext) {
        var font = graphicsContext.getFont();
        var textOutlineWidth = font.getSize2D() * textOutlineWidthRatio;
        return (int) (getBounds(graphicsContext).getWidth() + textOutlineWidth * 2);
    }

    @Override
    public int getHeight(Graphics2D graphicsContext) {
        var font = graphicsContext.getFont();
        var textOutlineWidth = font.getSize2D() * textOutlineWidthRatio;
        return (int) (getBounds(graphicsContext).getHeight() + textOutlineWidth * 2);
    }

    private Shape getShape(Graphics2D graphicsContext) {
        var font = graphicsContext.getFont();
        var fontRenderContext = graphicsContext.getFontRenderContext();
        var glyphVector = font.createGlyphVector(fontRenderContext, getText());
        return glyphVector.getOutline();
    }

    private Rectangle2D getBounds(Graphics2D graphicsContext) {
        return getShape(graphicsContext).getBounds2D();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            super.hashCode(),
            textFillColor,
            textOutlineColor,
            textOutlineWidthRatio
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        } else if (!super.equals(obj)) {
            return false;
        } else {
            var other = (OutlinedTextDrawable) obj;
            return Float.compare(other.textOutlineWidthRatio, textOutlineWidthRatio) == 0
                && Objects.equals(textFillColor, other.textFillColor)
                && Objects.equals(textOutlineColor, other.textOutlineColor);
        }
    }

    @Override
    public String toString() {
        return "OutlinedTextDrawable{"
            + "textFillColor=" + textFillColor
            + ", textOutlineColor=" + textOutlineColor
            + ", textOutlineWidthRatio=" + textOutlineWidthRatio
            + '}';
    }
}
