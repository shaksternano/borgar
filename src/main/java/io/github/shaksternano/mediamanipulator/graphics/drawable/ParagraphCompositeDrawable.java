package io.github.shaksternano.mediamanipulator.graphics.drawable;

import io.github.shaksternano.mediamanipulator.graphics.TextAlignment;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ParagraphCompositeDrawable extends BaseCompositeDrawable {

    private final TextAlignment ALIGNMENT;
    private final int MAX_WIDTH;
    @Nullable
    private final Integer MAX_HEIGHT;

    private static final Drawable SPACE = new TextDrawable(" ");

    public ParagraphCompositeDrawable(TextAlignment alignment, int maxWidth, @Nullable Integer maxHeight) {
        ALIGNMENT = alignment;
        MAX_WIDTH = Math.max(0, maxWidth);
        if (maxHeight == null) {
            MAX_HEIGHT = null;
        } else {
            MAX_HEIGHT = Math.max(0, maxHeight);
        }
    }

    @Override
    public void draw(Graphics2D graphics, int x, int y) {
        Font font = graphics.getFont();
        boolean needToResetFont = false;

        if (MAX_HEIGHT != null) {
            float sizeRatio = (float) MAX_HEIGHT / getHeight(graphics);
            if (sizeRatio < 1) {
                float newSize = font.getSize() * sizeRatio;
                graphics.setFont(font.deriveFont(newSize));
                needToResetFont = true;
            }
        }

        FontMetrics metrics = graphics.getFontMetrics();
        int lineHeight = metrics.getAscent() + metrics.getDescent();
        int lineSpace = metrics.getLeading();
        int lineWidth = 0;
        int lineX;
        int lineY = y;

        List<Drawable> currentLine = new ArrayList<>();

        for (Drawable part : getParts()) {
            try {
                part.resizeToHeight(lineHeight);
            } catch (UnsupportedOperationException ignored) {
            }

            int newLineWidth = lineWidth + part.getWidth(graphics);
            if (lineWidth > 0) {
                newLineWidth += SPACE.getWidth(graphics);
            }
            if (newLineWidth <= MAX_WIDTH) {
                currentLine.add(part);
                lineWidth = newLineWidth;
            } else {
                lineX = calculateTextXPosition(ALIGNMENT, x, lineWidth, MAX_WIDTH);

                int spaceWidth = SPACE.getWidth(graphics);
                if (ALIGNMENT == TextAlignment.JUSTIFY) {
                    spaceWidth += (MAX_WIDTH - lineWidth) / (currentLine.size() - 1);
                }

                for (Drawable linePart : currentLine) {
                    linePart.draw(graphics, lineX, lineY);
                    lineX += linePart.getWidth(graphics) + spaceWidth;
                }

                currentLine.clear();
                currentLine.add(part);
                lineWidth = part.getWidth(graphics);
                lineY += lineHeight + lineSpace;
            }
        }

        lineX = calculateTextXPosition(ALIGNMENT, x, lineWidth, MAX_WIDTH);
        for (Drawable linePart : currentLine) {
            linePart.draw(graphics, lineX, lineY);
            lineX += linePart.getWidth(graphics) + SPACE.getWidth(graphics);
        }

        if (needToResetFont) {
            graphics.setFont(font);
        }
    }

    private static int calculateTextXPosition(TextAlignment alignment, int x, int lineWidth, int maxWidth) {
        switch (alignment) {
            case CENTER -> x += (maxWidth - lineWidth) / 2;
            case RIGHT -> x += maxWidth - lineWidth;
        }

        return x;
    }

    @Override
    public int getWidth(Graphics2D graphicsContext) {
        FontMetrics metrics = graphicsContext.getFontMetrics();
        int lineHeight = metrics.getAscent() + metrics.getDescent();
        int lineWidth = 0;

        for (Drawable part : getParts()) {
            try {
                part.resizeToHeight(lineHeight);
            } catch (UnsupportedOperationException ignored) {
            }

            int newLineWidth = lineWidth + part.getWidth(graphicsContext);
            if (lineWidth > 0) {
                newLineWidth += SPACE.getWidth(graphicsContext);
            }
            if (newLineWidth <= MAX_WIDTH) {
                lineWidth = newLineWidth;
            } else {
                lineWidth = part.getWidth(graphicsContext);
            }
        }

        return lineWidth;
    }

    @Override
    public int getHeight(Graphics2D graphicsContext) {
        FontMetrics metrics = graphicsContext.getFontMetrics();
        int lineHeight = metrics.getAscent() + metrics.getDescent();
        int lineSpace = metrics.getLeading();
        int lineWidth = 0;
        int lineY = 0;

        for (Drawable part : getParts()) {
            try {
                part.resizeToHeight(lineHeight);
            } catch (UnsupportedOperationException ignored) {
            }

            int newLineWidth = lineWidth + part.getWidth(graphicsContext);
            if (lineWidth > 0) {
                newLineWidth += SPACE.getWidth(graphicsContext);
            }
            if (newLineWidth <= MAX_WIDTH) {
                lineWidth = newLineWidth;
            } else {
                lineWidth = part.getWidth(graphicsContext);
                lineY += lineHeight + lineSpace;
            }
        }

        lineY += lineHeight;
        return lineY;
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
        } else if (obj instanceof ParagraphCompositeDrawable other) {
            return super.equals(obj) &&
                    Objects.equals(ALIGNMENT, other.ALIGNMENT) &&
                    MAX_WIDTH == other.MAX_WIDTH &&
                    Objects.equals(MAX_HEIGHT, other.MAX_HEIGHT);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ALIGNMENT, MAX_WIDTH, MAX_HEIGHT);
    }
}
